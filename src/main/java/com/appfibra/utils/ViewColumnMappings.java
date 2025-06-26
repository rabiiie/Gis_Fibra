package com.appfibra.utils;

import java.util.Map;

public class ViewColumnMappings {

    public static Map<String, String> getAggregatorColumnsMap(String viewName) {
        switch (viewName.toLowerCase()) {
            case "contracts_list":
                return Map.ofEntries(
                    Map.entry("building_id", "c.building_id"),
                    Map.entry("subtype", "c.subtype"),
                    Map.entry("aufgabenstatus", "c.aufgabenstatus"),
                    Map.entry("access_location", "c.access_location"),
                    Map.entry("anschlussstatus", "m.anschlussstatus"),
                    Map.entry("tiefbaudatum", "m.tiefbaudatum"),
                    Map.entry("activeoperator_name_from_tri", "m.activeoperator_name_from_tri"),
                    Map.entry("outsource_name", "o.outsource_name"),
                    Map.entry("has_class", "has_class"),
                    Map.entry("home_id", "home_id"),
                    Map.entry("project_code", "project_code")                    
                );

            case "buildings_status_extended":
                return Map.ofEntries(
                    Map.entry("access_location", "access_location"),
                    Map.entry("pha",             "pha"),
                    Map.entry("has_class",       "building_has_class"),
                    Map.entry("building_status", "building_status"),
                    Map.entry("aufgabenstatus",  "aufgabenstatus")
                );

                
            case "baulist_highlights":
                return Map.ofEntries(
                  // clave UI → nombre columna real
                  Map.entry("home_id",          "home_id"),
                  Map.entry("building_id",      "building_id"),
                  Map.entry("order_id",         "order_id"),
                  Map.entry("order_date",       "order_date"),
                  Map.entry("project_code",     "project_code"),
                  Map.entry("project_name",     "project_name"),
                  Map.entry("area_pop",         "area_pop"),
                  Map.entry("city",             "city"),
                  Map.entry("tzip",             "tzip"),
                  Map.entry("street",           "street"),
                  Map.entry("house_number",     "house_number"),
                  Map.entry("suffix",           "suffix"),
                  Map.entry("unit",             "unit"),
                  Map.entry("completion_date",  "completion_date"),
                  Map.entry("connection_date",  "connection_date"),
                  Map.entry("delivery_status",  "delivery_status"),
                  Map.entry("reason_nc",        "reason_nc"),
                  Map.entry("provider",         "provider"),
                  Map.entry("co_id",            "co_id"),
                  Map.entry("firstname",        "firstname"),
                  Map.entry("name",             "name"),
                  Map.entry("phone",            "phone"),
                  Map.entry("cell_phone",       "cell_phone"),
                  Map.entry("mail",             "mail"),
                  Map.entry("task_status",      "task_status"),
                  Map.entry("group_name",       "group_name"),
                  Map.entry("catv",             "catv"),
                  Map.entry("kid",              "kid"),
                  Map.entry("hold_message",     "hold_message"),
                  Map.entry("subtype",          "subtype"),
                  Map.entry("network_type",     "network_type"),
                  Map.entry("change_type",      "change_type"),
                  Map.entry("change_week",      "change_week"),
                  Map.entry("change_year",      "change_year"),
                  Map.entry("order_week",       "order_week"),
                  Map.entry("order_year",       "order_year")
                );
                
            case "task_units":
            	return Map.ofEntries(
	            	    Map.entry("building_id", "building_id"),
	            	    Map.entry("project_number", "project_number"),
	            	    Map.entry("assignment_number", "assignment_number"),
	            	    Map.entry("ap_from_tri", "ap_from_tri"),
	            	    Map.entry("dp", "dp"),
	            	    Map.entry("street", "street"),
	            	    Map.entry("zip_code", "zip_code"),
	            	    Map.entry("house_number", "house_number"),
	            	    Map.entry("house_number_addition", "house_number_addition"),
	            	    Map.entry("room", "room"),
	            	    Map.entry("order_template", "order_template"),
	            	    Map.entry("status", "status"),
	            	    Map.entry("anschlussstatus", "anschlussstatus"),
	            	    Map.entry("grundna", "grundna"),
	            	    Map.entry("garden_bore_done", "garden_bore_done"),
	            	    Map.entry("url_faser_am_hauswand", "url_faser_am_hauswand"),
	            	    Map.entry("hv_kabellange_einfuhrung_hup", "hv_kabellange_einfuhrung_hup"),
	            	    Map.entry("servicepaket_nicht_verreinbart_instaliert", "servicepaket_nicht_verreinbart_instaliert"),
	            	    Map.entry("montagetechniker", "montagetechniker"),
	            	    Map.entry("gartenbohrung_fertig", "gartenbohrung_fertig"),
	            	    Map.entry("hc_hup_standort_hup_another", "hc_hup_standort_hup_another"),
	            	    Map.entry("hv_standort_hup", "hv_standort_hup"),
	            	    Map.entry("status_1", "status_1"),
	            	    Map.entry("number_of_we", "number_of_we"),
	            	    Map.entry("hc_hup_standort_hup_another_1", "hc_hup_standort_hup_another_1"),
	            	    Map.entry("name_des_hausbeghers", "name_des_hausbeghers"),
	            	    Map.entry("anzahl_eingeblasene_hp_adressen", "anzahl_eingeblasene_hp_adressen"),
	            	    Map.entry("spleisse_ap_bereit", "spleisse_ap_bereit"),
	            	    Map.entry("spleisse_dp_bereit", "spleisse_dp_bereit"),
	            	    Map.entry("fertigstellungsdatum_teilstuck", "fertigstellungsdatum_teilstuck"),
	            	    Map.entry("hv_date", "hv_date"),
	            	    Map.entry("erklarung_status", "erklarung_status"),
	            	    Map.entry("erklarungsstatus", "erklarungsstatus"),
	            	    Map.entry("aktivation_bemerkung", "aktivation_bemerkung"),
	            	    Map.entry("ready_108", "ready_108"),
	            	    Map.entry("subtype_from_tri", "subtype_from_tri"),
	            	    Map.entry("serviceprovider_from_tri", "serviceprovider_from_tri"),
	            	    Map.entry("serviceprovider_name_from_tri", "serviceprovider_name_from_tri"),
	            	    Map.entry("anzahl_we", "anzahl_we"),
	            	    Map.entry("bis_we_anzahl", "bis_we_anzahl"),
	            	    Map.entry("name", "name"),
	            	    Map.entry("status_updated_at", "status_updated_at"),
	            	    Map.entry("action_holder", "action_holder"),
	            	    Map.entry("active_operator", "active_operator"),
	            	    Map.entry("tiefbau_date", "tiefbau_date"),
	            	    Map.entry("place", "place"),
	            	    Map.entry("status_hausbegehung", "status_hausbegehung"),
	            	    Map.entry("status_hausbegehung_hv_s_from_house", "status_hausbegehung_hv_s_from_house")
	            	);




            case "planner":
                return Map.ofEntries(
                	    Map.entry("projektcode_intern", "projektcode_intern"),
                	    Map.entry("projektname", "projektname"),
                	    Map.entry("teilpolygon", "teilpolygon"),
                	    Map.entry("dp_name", "dp_name"),
                	    Map.entry("kabel_id_from_engineering", "kabel_id_from_engineering"),
                	    Map.entry("tzip", "tzip"),
                	    Map.entry("strasse", "strasse"),
                	    Map.entry("hausnummer", "hausnummer"),
                	    Map.entry("hausnummernzusatz", "hausnummernzusatz"),
                	    Map.entry("unit", "unit"),
                	    Map.entry("grundna", "grundna"),
                	    Map.entry("zustimmung", "zustimmung"),
                	    Map.entry("anschlussstatus", "anschlussstatus"),
                	    Map.entry("baulist_datum", "baulist_datum"),
                	    Map.entry("civiel_startdatum", "civiel_startdatum"),
                	    Map.entry("tiefbaudatum", "tiefbaudatum"),
                	    Map.entry("civiel_einddatum", "civiel_einddatum"),
                	    Map.entry("plandatum", "plandatum"),
                	    Map.entry("reihe_from_engineering", "reihe_from_engineering"),
                	    Map.entry("schrank_from_engineering", "schrank_from_engineering"),
                	    Map.entry("block_from_engineering", "block_from_engineering"),
                	    Map.entry("odf_catv_from_engineering", "odf_catv_from_engineering"),
                	    Map.entry("odf_catv_position_from_engineering", "odf_catv_position_from_engineering"),
                	    Map.entry("odf_ip_from_engineering", "odf_ip_from_engineering"),
                	    Map.entry("odf_ip_position_from_engineering", "odf_ip_position_from_engineering"),
                	    Map.entry("has_ip_fiber", "has_ip_fiber"),
                	    Map.entry("has_tv_fiber", "has_tv_fiber"),
                	    Map.entry("gebaeudetyp", "gebaeudetyp"),
                	    Map.entry("order_template", "order_template"),
                	    Map.entry("datum_hausbegehung", "datum_hausbegehung"),
                	    Map.entry("status_hausbegehung", "status_hausbegehung"),
                	    Map.entry("has_aktivierung_geplant", "has_aktivierung_geplant"),
                	    Map.entry("kunde_zufrieden_installation", "kunde_zufrieden_installation"),
                	    Map.entry("auftragsnummer_extern", "auftragsnummer_extern"),
                	    Map.entry("totale_lengte", "totale_lengte"),
                	    Map.entry("dp_tiefbau_fertig", "dp_tiefbau_fertig"),
                	    Map.entry("graaflengte_produktion_totaal", "graaflengte_produktion_totaal"),
                	    Map.entry("nog_te_graven", "nog_te_graven"),
                	    Map.entry("auftragsnummer", "auftragsnummer"),
                	    Map.entry("tknr", "tknr"),
                	    Map.entry("farbe_rohre", "farbe_rohre"),
                	    Map.entry("datum_hausanschluss", "datum_hausanschluss"),
                	    Map.entry("fertigstellungsdatum_teilstueck", "fertigstellungsdatum_teilstueck"),
                	    Map.entry("ready_108", "ready_108"),
                	    Map.entry("abhaengigkeit", "abhaengigkeit"),
                	    Map.entry("gartenbohrung_durchgefuehrt", "gartenbohrung_durchgefuehrt"),
                	    Map.entry("hc_hup_standort_hup", "hc_hup_standort_hup"),
                	    Map.entry("activeoperator_name_from_tri", "activeoperator_name_from_tri"),
                	    Map.entry("bautyp", "bautyp"),
                	    Map.entry("building_id", "building_id")
                	);
            case "activations_highlights":
                return Map.ofEntries(
                	Map.entry("home_id", "home_id"),
                	Map.entry("building_id", "building_id"),
                	Map.entry("access_location", "access_location"),
                    Map.entry("bestellnummer", "bestellnummer"),
                    Map.entry("order_date", "order_date"),
                    Map.entry("aufgabenstatus", "aufgabenstatus"),
                    Map.entry("typ", "typ"),
                    Map.entry("week", "week"),
                    Map.entry("year", "year"),
                    Map.entry("subtype", "subtype")
                );
                
            case "ar_web":
            	return Map.ofEntries(
            		Map.entry("home_id", "home_id"),
            		Map.entry("building_id", "building_id"),
            		Map.entry("cable_id", "cable_id"),
            		Map.entry("plot", "plot"),
            		Map.entry("tzip", "tzip"),
            		Map.entry("number", "number"),
            		Map.entry("suffix", "suffix"),
            		Map.entry("unit", "unit"),
            		Map.entry("street", "street"),
            		Map.entry("city", "city"),
            		Map.entry("date_last_update", "date_last_update"),
            		Map.entry("last_updated_by", "last_updated_by"),
            		Map.entry("start_date_civil_work", "start_date_civil_work"),
            		Map.entry("plan_date_has", "plan_date_has"),
            		Map.entry("has_date", "has_date"),
            		Map.entry("hp_plandate", "hp_plandate"),
            		Map.entry("delivery_date_passive", "delivery_date_passive"),
            		Map.entry("delivery_status", "delivery_status"),
            		Map.entry("reason_nc", "reason_nc"),
            		Map.entry("area_pop", "area_pop"),
            		Map.entry("row", "row"),
            		Map.entry("frame", "frame"),
            		Map.entry("block", "block"),
            		Map.entry("odf", "odf"),
            		Map.entry("odf_position", "odf_position"),
            		Map.entry("odf_status", "odf_status"),
            		Map.entry("odf_last_update", "odf_last_update"),
            		Map.entry("connection_id", "connection_id"),
            		Map.entry("odfcatv", "odfcatv"),
            		Map.entry("odfcatv_position", "odfcatv_position"),
            		Map.entry("odfcatv_status", "odfcatv_status"),
            		Map.entry("odfcatv_last_update", "odfcatv_last_update"),
            		Map.entry("connection_id_catv", "connection_id_catv"),
            		Map.entry("ftu_type", "ftu_type"),
            		Map.entry("permission_to_build", "permission_to_build"),
            		Map.entry("building_type", "building_type"),
            		Map.entry("type_of_construction", "type_of_construction"),
            		Map.entry("project_code", "project_code"),
            		Map.entry("project_name", "project_name"),
            		Map.entry("connection_area_number", "connection_area_number"),
            		Map.entry("connection_area_name", "connection_area_name"),
            		Map.entry("contractor", "contractor"),
            		Map.entry("project_construction_type", "project_construction_type"),
            		Map.entry("remark", "remark"),
            		Map.entry("strand_id", "strand_id"),
            		Map.entry("throughput_dependency", "throughput_dependency"),
            		Map.entry("floor", "floor"),
            		Map.entry("flat_position", "flat_position"),
            		Map.entry("netbuildtype", "netbuildtype"),
            		Map.entry("network_type", "network_type"),
            		Map.entry("projectstatus", "projectstatus")
            	);




            default:
                throw new IllegalArgumentException("Vista no válida para agregación: " + viewName);
        }
        
    }
}
