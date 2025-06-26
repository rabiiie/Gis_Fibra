package com.appfibra.Controller;

import com.appfibra.service.CivilWorksService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.PDFont;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/civil-works")
public class CivilWorksExportController {

    private final CivilWorksService civilWorksService;

    @Autowired
    public CivilWorksExportController(CivilWorksService civilWorksService) {
        this.civilWorksService = civilWorksService;
    }

    /**
     * Exportar en Excel con "Length (m)" como número y formato europeo (`,` como separador decimal).
     */
    @GetMapping("/export/excel")
    public ResponseEntity<ByteArrayResource> exportExcel(@RequestParam(required = false) Map<String, String> filters) throws IOException {
        List<Map<String, Object>> data = civilWorksService.getFilteredData("Engineering", filters);
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("BOM Civil Works");

        // Congelar la cabecera en la tercera fila (Index = 3)
        sheet.createFreezePane(0, 3);

        // Crear estilos
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle numberStyle = createNumberStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);

        // Fila 0 y 1 fusionadas para la fecha
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 8));
        Row row0 = sheet.createRow(0);
        row0.setHeightInPoints(25);
        Cell dateCell = row0.createCell(0);
        dateCell.setCellValue("Date: " + LocalDate.now().toString());
        dateCell.setCellStyle(dateStyle);

        // Fila 2: Cabecera de la tabla
        Row headerRow = sheet.createRow(2);
        String[] columns = {"Project", "POP", "DP", "Street", "TZIP", "Type", "Spec", "Length (m)", "Village"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // Llenar datos desde la fila 3
        int rowNum = 3;
        for (Map<String, Object> mapRow : data) {
            Row excelRow = sheet.createRow(rowNum++);
            excelRow.createCell(0).setCellValue(safeString(mapRow.get("project")));
            excelRow.createCell(1).setCellValue(safeString(mapRow.get("pop")));
            excelRow.createCell(2).setCellValue(safeString(mapRow.get("dp")));
            excelRow.createCell(3).setCellValue(safeString(mapRow.get("street")));
            excelRow.createCell(4).setCellValue(safeString(mapRow.get("tzip")));
            excelRow.createCell(5).setCellValue(safeString(mapRow.get("type")));
            excelRow.createCell(6).setCellValue(safeString(mapRow.get("spec")));

            // Convertir "Length (m)" a número
            Cell lengthCell = excelRow.createCell(7);
            Object lengthValue = mapRow.get("length_meters");
            if (lengthValue instanceof Number) {
                lengthCell.setCellValue(((Number) lengthValue).doubleValue());
            } else {
                try {
                    lengthCell.setCellValue(Double.parseDouble(lengthValue.toString()));
                } catch (NumberFormatException e) {
                    lengthCell.setCellValue(0);
                }
            }
            lengthCell.setCellStyle(numberStyle); // Aplicar estilo numérico

            excelRow.createCell(8).setCellValue(safeString(mapRow.get("village")));

            // Aplicar estilo a todas las celdas de datos excepto la de "Length (m)"
            for (int col = 0; col < columns.length; col++) {
                if (col != 7) {
                    excelRow.getCell(col).setCellStyle(dataStyle);
                }
            }
        }

        // Autoajustar columnas
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Convertir a ByteArray y devolver como archivo
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        ByteArrayResource resource = new ByteArrayResource(out.toByteArray());
        HttpHeaders headersHttp = new HttpHeaders();
        headersHttp.setContentDisposition(ContentDisposition.builder("attachment").filename("BOM_Civil_Works.xlsx").build());
        headersHttp.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok().headers(headersHttp).body(resource);
    }

    /**
     * Estilo para la cabecera de la tabla (fondo gris, negrita, bordes).
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        setThinBorders(style);
        return style;
    }

    /**
     * Estilo para los datos en la tabla.
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setThinBorders(style);
        return style;
    }

    /**
     * Estilo para los números (con formato europeo `#,##0.00`).
     */
    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00")); // Formato con `,` como separador decimal
        setThinBorders(style);
        return style;
    }

    /**
     * Estilo para la celda de fecha.
     */
    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }

    /**
     * Aplica bordes finos a un CellStyle.
     */
    private void setThinBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
    
    @GetMapping("/export/pdf")
    public ResponseEntity<ByteArrayResource> exportPDF(@RequestParam(required = false) Map<String, String> filters) throws IOException {
        List<Map<String, Object>> data = civilWorksService.getFilteredData("Engineering", filters);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float margin = 50;
                float yStart = page.getMediaBox().getHeight() - margin;
                float yPosition = yStart;
                float rowHeight = 20f;

                // **Título del documento**
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Civil Works Report");
                contentStream.endText();
                yPosition -= 30;

                // **Fecha del reporte**
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Date: " + LocalDate.now());
                contentStream.endText();
                yPosition -= 20;

                // **Encabezado de la tabla**
                String[] columns = {"Project", "POP", "DP", "Street", "TZIP", "Type", "Spec", "Length (m)", "Village"};
                float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
                float columnWidth = tableWidth / columns.length;

                // Dibujar encabezado
                for (String column : columns) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(column);
                    contentStream.endText();
                    margin += columnWidth;
                }
                yPosition -= rowHeight;

                // **Dibujar filas de datos**
                for (Map<String, Object> row : data) {
                    margin = 50; // Reiniciar margen izquierdo

                    for (String column : columns) {
                        contentStream.beginText();
                        contentStream.newLineAtOffset(margin, yPosition);
                        contentStream.showText(safeString(row.get(column.toLowerCase())));
                        contentStream.endText();
                        margin += columnWidth;
                    }
                    yPosition -= rowHeight;
                }
            }

            // **Guardar el PDF en un ByteArrayOutputStream**
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);

            // **Devolver el PDF como respuesta**
            ByteArrayResource resource = new ByteArrayResource(out.toByteArray());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("attachment").filename("civil_work_export.pdf").build());

            return ResponseEntity.ok().headers(headers).body(resource);
        }
    }

    /**
     * Aplica el formato decimal con ',' como separador
     */
    private String formatDecimal(Object value, DecimalFormat decimalFormat) {
        if (value instanceof Number) {
            return decimalFormat.format(((Number) value).doubleValue());
        }
        return safeString(value);
    }

    /**
     * Evita valores nulos, devolviendo "" en su lugar.
     */
    private String safeString(Object obj) {
        return obj == null ? "" : obj.toString();
    }
}
