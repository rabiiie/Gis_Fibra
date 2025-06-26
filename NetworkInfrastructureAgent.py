import os
import sys
import json
import logging
import ast
from tenacity import retry, stop_after_attempt, wait_exponential
from langchain_community.utilities import SQLDatabase
from langchain_community.agent_toolkits.sql.toolkit import SQLDatabaseToolkit
from langchain_openai import ChatOpenAI
from langchain_community.agent_toolkits.sql.base import create_sql_agent
from sqlalchemy.sql import text

# Configurar logging para debug
logging.basicConfig(level=logging.DEBUG, format='%(asctime)s [%(levelname)s] %(message)s')

class NetworkInfrastructureAgent:
    def __init__(self, db_uri: str, api_key: str):
        logging.debug(f"Inicializando NetworkInfrastructureAgent con db_uri: {db_uri}")
        self.db = self._init_database(db_uri)
        self.llm = self._init_llm(api_key)
        self.agent = self._create_agent()

    def _init_database(self, db_uri: str) -> SQLDatabase:
        logging.debug(f"Conectando a PostgreSQL en: {db_uri}")
        try:
            return SQLDatabase.from_uri(
                db_uri,
                include_tables=[
                    'clients', 'projects', 'pops', 'dps', 'streets', 'addresses',
                    'active_equipment', 'cables', 'ducts', 'manholes', 'civil_works',
                    'protections', 'inventory', 'microducts'
                ],
                sample_rows_in_table_info=3,
                engine_args={
                    'pool_size': 15,
                    'connect_args': {'connect_timeout': 20}
                }
            )
        except Exception as e:
            logging.error(f"Error conectando a PostgreSQL: {e}")
            sys.exit(1)

    def _init_llm(self, api_key: str) -> ChatOpenAI:
        logging.debug("Inicializando ChatOpenAI")
        return ChatOpenAI(
            temperature=0.1,
            model="deepseek-chat",
            openai_api_key=api_key,
            openai_api_base="https://api.deepseek.com/v1",
            max_tokens=2500,
            frequency_penalty=0.2,
            presence_penalty=0.1,
            stream=False
        )

    def _create_agent(self):
        logging.debug("Creando el agente SQL")
        toolkit = SQLDatabaseToolkit(
            db=self.db,
            llm=self.llm,
            custom_table_info=self._get_table_descriptions()
        )
        return create_sql_agent(
            llm=self.llm,
            toolkit=toolkit,
            agent_type="openai-functions",
            verbose=True,
            extra_prompt_messages=[self._get_system_prompt()],
            max_execution_time=60,
            handle_parsing_errors=True
        )

    def _get_table_descriptions(self) -> dict:
        logging.debug("Obteniendo descripciones de las tablas")
        return {
            'clients': (
                "Tabla de clientes.\n"
                "Campos:\n"
                " - client_id (PK)\n"
                " - name (nombre del cliente)\n"
                " - logo_url\n"
                "Relación: projects.client_id"
            ),
            'projects': (
                "Tabla de proyectos.\n"
                "Campos:\n"
                " - project_id (PK)\n"
                " - client_id (FK a clients.client_id)\n"
                " - name (nombre del proyecto)\n"
                " - engineering_pd, construction_pd, construction_manager, engineering_manager, pd, country, phase, subcontractor\n"
                "Relación: pops.project_id"
            ),
            'pops': (
                "Tabla de Puntos de Presencia (POP).\n"
                "Campos:\n"
                " - pop_id (PK)\n"
                " - name\n"
                " - project_id (FK a projects.project_id)\n"
                "Relación: dps.pop_id"
            ),
            'dps': (
                "Tabla de Puntos de Distribución (DP).\n"
                "Campos:\n"
                " - dp_id (PK)\n"
                " - pop_id (FK a pops.pop_id)\n"
                " - project_id (posible FK a projects.project_id)\n"
                " - name (nombre del DP)\n"
                "Relación: streets.dp_id"
            ),
            'streets': (
                "Tabla de calles.\n"
                "Campos:\n"
                " - street_id (PK)\n"
                " - dp_id (FK a dps.dp_id, nullable)\n"
                " - project_id (FK a projects.project_id)\n"
                " - name\n"
                " - village\n"
                " - updated_at\n"
                " - tzip\n"
                "Relación: addresses.street_id, civil_works.street_id"
            ),
            'addresses': (
                "Tabla de direcciones de servicio.\n"
                "Campos:\n"
                " - address_id (PK)\n"
                " - street_id (FK a streets.street_id)\n"
                " - project_id (FK a projects.project_id)\n"
                " - dp_id (FK a dps.dp_id)\n"
                " - pop_id (FK a pops.pop_id)\n"
                " - address_name, house_number, prefix, units, client_name, etc.\n"
                " - latitude, longitude, subscription_date, status, activations, contracts, etc.\n"
                "Relación: microducts.service_address_id"
            ),
            'active_equipment': (
                "Tabla de equipos activos.\n"
                "Posibles campos: equipment_id (PK), pop_id, dp_id, status."
            ),
            'cables': (
                "Tabla de cables.\n"
                "Campos:\n"
                " - cable_id (PK)\n"
                " - cable_type\n"
                " - start_pop_id, end_dp_id\n"
                " - fibers_count, blown, spliced\n"
                " - source, status\n"
                " - created_at, updated_at\n"
                "Relación con pops y dps."
            ),
            'ducts': (
                "Tabla de ductos principales.\n"
                " - duct_id (PK)\n"
                " - start_manhole_id, end_manhole_id (FK a manholes)\n"
                " - material, diameter_mm, length_meters, source, status\n"
                " - created_at, updated_at\n"
            ),
            'manholes': (
                "Tabla de registros civiles (manholes).\n"
                " - manhole_id (PK)\n"
                " - location_type, capacity, material\n"
                " - status, source\n"
                " - created_at, updated_at\n"
            ),
            'civil_works': (
                "Tabla de obras civiles.\n"
                " - civil_work_id (PK)\n"
                " - dp_id (FK a dps.dp_id)\n"
                " - pop_id (FK a pops.pop_id)\n"
                " - street_id (FK a streets.street_id)\n"
                " - type, property, diameter_mm, spec, length_meters, tzip\n"
                " - source, status, created_at, updated_at, village, feature_id\n"
            ),
            'protections': (
                "Tabla de protecciones.\n"
                " - protection_id (PK)\n"
                " - dp_id, pop_id, street_id\n"
                " - name, protection_type, description\n"
                " - source, status\n"
                " - created_at, updated_at\n"
            ),
            'inventory': (
                "Tabla de inventario.\n"
                " - item_id (PK)\n"
                " - name, type, quantity, unit\n"
                " - pop_id, dp_id\n"
            ),
            'microducts': (
                "Tabla de microductos.\n"
                " - microduct_id (PK)\n"
                " - duct_id (FK a ducts.duct_id)\n"
                " - service_address_id (FK a addresses.address_id)\n"
                " - color\n"
            )
        }

    def _get_system_prompt(self) -> str:
        return """
        Eres un especialista en redes FTTH con acceso a las siguientes tablas y relaciones:
        - clients: Contiene información de clientes. Relación: projects.client_id
        - projects: Proyectos de infraestructura. Relación: pops.project_id
        - pops: Puntos de presencia. Relación: dps.pop_id
        - dps: Puntos de distribución. Relación: streets.dp_id
        - streets: Calles. Relación: addresses.street_id
        - addresses: Direcciones de servicio. Relación: microducts.service_address_id
        - active_equipment: Equipos activos. Relación: pops.pop_id y dps.dp_id
        - cables: Cables de conexión. Relación: pops.pop_id y dps.dp_id
        - ducts: Ductos principales. Relación: manholes.manhole_id
        - manholes: Registros civiles. Relación: ducts.start_manhole_id y ducts.end_manhole_id
        - civil_works: Obras civiles. Relación: dps.dp_id, pops.pop_id, streets.street_id
        - protections: Protecciones. Relación: dps.dp_id, pops.pop_id, streets.street_id
        - inventory: Inventario. Relación: pops.pop_id y dps.dp_id
        - microducts: Microductos. Relación: ducts.duct_id y addresses.address_id

        Reglas estrictas:
        1. NO usar information_schema bajo ninguna circunstancia.
        2. Usar solo las tablas y relaciones proporcionadas.
        3. Priorizar JOINs usando índices: client_id, project_id, pop_id, dp_id.
        4. Incluir cláusulas WHERE tempranas para optimización.
        5. Limitar resultados a 1000 registros con LIMIT.
        6. Proporcionar explicación técnica breve en el 'Final Answer'.
        7. Verifica los tipos de datos antes de ejecutar la consulta.
        8. Si comparas valores en una columna de tipo VARCHAR o TEXT, usa comillas simples (') para tratarlos como cadenas de texto.

        Importante: Evitar columnas que NO existan (ej. 'client_name', 'project_name', etc.). 
        Utilizar las existentes: clients.name, projects.name, pops.name, etc.
        """

    @retry(stop=stop_after_attempt(3), wait=wait_exponential(multiplier=1, min=2, max=60))
    def execute_query(self, question: str) -> dict:
        try:
            full_prompt = f"""
            Contexto de Red FTTH:
            {self._get_system_prompt()}
            
            Pregunta: {question}
            
            Requisitos:
            - Usar solo las tablas permitidas (NO consultar information_schema).
            - Incluir metadatos de proyecto cuando aplique.
            - Respetar restricciones de eliminación (ON DELETE CASCADE/SET NULL).
            - Validar tipos de datos.
            - Recuerda usar INNER JOIN ON, no USING.
            - Solo usar columnas reales: 
              * clients.name, 
              * projects.name, 
              * pops.name,
              * dps.name, 
              * civil_works.type, 
              * etc.
            """
            logging.debug("Enviando prompt completo al agente:")
            logging.debug(full_prompt)
            result = self.agent.invoke(full_prompt)
            return self._format_result(result)
        except Exception as e:
            logging.exception("Error al ejecutar la consulta:")
            return {
                "error": str(e),
                "advice": "Verificar relaciones entre entidades o la sintaxis SQL generada"
            }

    def _format_result(self, raw_result) -> dict:
        # Convertir raw_result a cadena si no es dict
        if isinstance(raw_result, dict):
            raw_result_str = raw_result.get("content", str(raw_result))
        else:
            raw_result_str = str(raw_result)

        # Extraer la consulta SQL del bloque ```sql ... ```
        query = "No identificada"
        if "```sql" in raw_result_str:
            parts = raw_result_str.split("```sql")
            if len(parts) > 1:
                query_block = parts[1].split("```")[0]
                query = query_block.strip()

        # Decodificar secuencias de escape (por ejemplo, "\\n") a saltos de línea reales
        query = query.encode('utf-8').decode('unicode_escape').strip()

        # Extraer la "Final Answer" si existe (aunque luego se ejecutará la consulta)
        if "Final Answer:" in raw_result_str:
            final_answer = raw_result_str.split("Final Answer:")[-1].strip()
        else:
            final_answer = raw_result_str.strip()

        observations = self._extract_sql_observations(query)

        # Ejecutar la consulta en la DB
        try:
            logging.debug(f"Ejecutando consulta SQL:\n{query}")
            result = self.db.run(query)
            logging.debug(f"Resultado crudo de la DB: {result}")

            # Si result es una cadena, intenta evaluarla para convertirla en objeto Python
            if isinstance(result, str):
                try:
                    result = ast.literal_eval(result)
                except Exception as e:
                    logging.error(f"Error al evaluar el resultado: {e}")

            if result and len(result) > 0:
                if isinstance(result[0], dict):
                    first_row = result[0]
                    col_name = list(first_row.keys())[0]
                    final_answer = first_row[col_name]
                elif isinstance(result[0], (list, tuple)):
                    final_answer = result[0][0]
                else:
                    final_answer = result[0]
                final_answer = str(final_answer)
            else:
                final_answer = "No se encontraron resultados"
        except Exception as e:
            logging.error(f"Error al ejecutar la consulta SQL: {e}")
            final_answer = "Error al ejecutar la consulta SQL"

        return {
            "technical_answer": final_answer,
            "execution_details": {
                "query": query,
                "raw_response": raw_result_str,
                "observations": observations
            }
        }

    def _extract_sql_observations(self, query: str) -> list:
        observations = []
        upper_query = query.upper()
        if "ON DELETE CASCADE" in upper_query:
            observations.append("CUIDADO: Esta operación puede eliminar registros relacionados en cascada")
        if "LEFT JOIN" in upper_query:
            observations.append("JOIN opcional detectado - Puede incluir registros con valores NULL")
        if "COALESCE" in upper_query:
            observations.append("Manejo de valores NULL mediante COALESCE")
        if "GROUP BY" in upper_query:
            observations.append("Agrupación de resultados detectada - verificar métricas calculadas")
        return observations

if __name__ == "__main__":
    try:
        logging.debug("Inicio del script principal")
        if len(sys.argv) > 1:
            arg = sys.argv[1]
            logging.debug(f"Argumento recibido: {arg}")
            if os.path.isfile(arg):
                with open(arg, 'r', encoding='utf-8') as f:
                    payload = json.load(f)
            else:
                try:
                    payload = json.loads(arg)
                except json.JSONDecodeError as e:
                    logging.error(f"Error al decodificar JSON: {e}")
                    sys.exit(1)
            logging.debug(f"Payload JSON:\n{json.dumps(payload, indent=2)}")
            question = payload.get("question", "")
            DB_URI = "postgresql+psycopg2://postgres:Rsamadi7@localhost:5432/soluciones"
            API_KEY = "sk-b8b79c09863e4ca8a2da4a47bd8e283b"

            agent = NetworkInfrastructureAgent(db_uri=DB_URI, api_key=API_KEY)
            result = agent.execute_query(question)
            output_json = json.dumps(result)
            logging.debug("Resultado final del agente:")
            logging.debug(output_json)
            print(output_json)
        else:
            logging.debug("No se recibieron argumentos. Ejecutando pruebas predefinidas.")
            agent = NetworkInfrastructureAgent(
                db_uri="postgresql+psycopg2://postgres:Rsamadi7@localhost:5432/soluciones",
                api_key="sk-b8b79c09863e4ca8a2da4a47bd8e283b"
            )
            test_queries = [
                "Listar obras civiles con sus POPs y DPs asociados, incluyendo clientes",
                "Calcular cantidad de direcciones sin microductos por proyecto activo",
                "Obtener inventario de POPs con protección y sin equipos activos",
                "Identificar cables con ambos extremos en el mismo proyecto",
                "Auditar relaciones huérfanas en addresses sin street o dp válido"
            ]
            for i, query in enumerate(test_queries, 1):
                logging.debug(f"\nTEST #{i}: {query}")
                response = agent.execute_query(query)
                logging.debug(f"Respuesta Técnica:\n{response['technical_answer']}")
                logging.debug(f"Detalles SQL:\n{response['execution_details']['query']}")
                logging.debug(f"Observaciones:\n{response['execution_details']['observations']}")
    except Exception as e:
        logging.exception("Error en la ejecución del script:")
        print(f"Error en la ejecución del script: {e}")
