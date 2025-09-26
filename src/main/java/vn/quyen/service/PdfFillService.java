package vn.quyen.service;

import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Date;
import java.util.List;

public class PdfFillService {

    public static String fill(String pdfBase64, String jsonData, boolean flatten) throws Exception {
        byte[] pdfBytes = Base64.getDecoder().decode(pdfBase64);
        JSONObject json = new JSONObject(jsonData);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfBytes));
        PdfStamper stamper = new PdfStamper(reader, outputStream);
        AcroFields form = stamper.getAcroFields();

        for (String fieldName : form.getFields().keySet()) {
            Object rawValue = json.has(fieldName) ? json.get(fieldName) : null;
            int fieldType = form.getFieldType(fieldName);
            String typeName = getFieldTypeName(fieldType);

            PdfDictionary dict = form.getFieldItem(fieldName).getMerged(0);
            PdfName ft = dict.getAsName(PdfName.FT);
            String ftName = ft != null ? ft.toString().replace("/", "") : "—";

            String layout = "—";
            if ("Btn".equals(ftName)) {
                layout = getButtonLayout(dict);
            }

            // System.out.printf("🔍 Field: %-20s | Type: %-2d (%-10s) | FT: %-4s | Layout:
            // %-15s | Value: %s%n",
            // fieldName, fieldType, typeName, ftName, layout, rawValue);

            // Handle PushButton image
            if ("Btn".equals(ftName) && rawValue != null && isBase64Image(rawValue.toString())) {
                List<AcroFields.FieldPosition> positions = form.getFieldPositions(fieldName);
                if (positions != null && !positions.isEmpty()) {
                    AcroFields.FieldPosition pos = positions.get(0);
                    PushbuttonField button = new PushbuttonField(stamper.getWriter(), pos.position, fieldName);
                    Image img = Image.getInstance(Base64.getDecoder().decode(rawValue.toString()));
                    button.setImage(img);
                    button.setLayout(PushbuttonField.LAYOUT_ICON_ONLY);
                    button.setScaleIcon(PushbuttonField.SCALE_ICON_ALWAYS);
                    button.setProportionalIcon(true);
                    PdfFormField newBtn = button.getField();
                    form.replacePushbuttonField(fieldName, newBtn);
                }
                continue;
            }

            switch (fieldType) {
                case AcroFields.FIELD_TYPE_CHECKBOX -> {
                    String[] states = form.getAppearanceStates(fieldName);
                    String onValue = getCheckboxOnValue(states);
                    String finalValue = isTruthy(rawValue) ? onValue : "Off";
                    form.setField(fieldName, finalValue);
                }
                case AcroFields.FIELD_TYPE_RADIOBUTTON,
                        AcroFields.FIELD_TYPE_COMBO -> {
                    form.setField(fieldName, rawValue != null ? rawValue.toString() : "");
                }
                case AcroFields.FIELD_TYPE_LIST -> {
                    if (rawValue instanceof JSONArray arr) {
                        String[] values = new String[arr.length()];
                        for (int i = 0; i < arr.length(); i++) {
                            values[i] = arr.getString(i).trim();
                        }
                        form.setListSelection(fieldName, values);
                    } else {
                        form.setField(fieldName, rawValue != null ? rawValue.toString() : "");
                    }
                }
                case AcroFields.FIELD_TYPE_TEXT, AcroFields.FIELD_TYPE_NONE -> {
                    String value = rawValue != null ? rawValue.toString() : "";
                    form.setField(fieldName, value);
                }
                default -> {
                    String value = rawValue != null ? rawValue.toString() : "";
                    form.setField(fieldName, value);
                }
            }
        }

        if (flatten) {
            stamper.setFormFlattening(true);
        }

        stamper.close();
        reader.close();

        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    private static String getFieldTypeName(int type) {
        return switch (type) {
            case AcroFields.FIELD_TYPE_CHECKBOX -> "Checkbox";
            case AcroFields.FIELD_TYPE_RADIOBUTTON -> "Radio";
            case AcroFields.FIELD_TYPE_COMBO -> "Combo";
            case AcroFields.FIELD_TYPE_LIST -> "List";
            case AcroFields.FIELD_TYPE_TEXT -> "Text";
            case AcroFields.FIELD_TYPE_SIGNATURE -> "Signature";
            case AcroFields.FIELD_TYPE_NONE -> "None";
            case AcroFields.FIELD_TYPE_PUSHBUTTON -> "PushButton";
            default -> "Unknown";
        };
    }

    private static String getButtonLayout(PdfDictionary dict) {
        PdfNumber layout = dict.getAsNumber(PdfName.TI);
        if (layout == null)
            return "—";
        return switch (layout.intValue()) {
            case PushbuttonField.LAYOUT_LABEL_ONLY -> "Text Only";
            case PushbuttonField.LAYOUT_ICON_ONLY -> "Icon Only";
            case PushbuttonField.LAYOUT_ICON_TOP_LABEL_BOTTOM -> "Icon Top + Text Bottom";
            case PushbuttonField.LAYOUT_LABEL_TOP_ICON_BOTTOM -> "Text Top + Icon Bottom";
            case PushbuttonField.LAYOUT_ICON_LEFT_LABEL_RIGHT -> "Icon Left + Text Right";
            case PushbuttonField.LAYOUT_LABEL_LEFT_ICON_RIGHT -> "Text Left + Icon Right";
            case PushbuttonField.LAYOUT_LABEL_OVER_ICON -> "Text Over Icon";
            default -> "Unknown";
        };
    }

    private static boolean isBase64Image(String val) {
        return val.length() > 100 && (val.startsWith("iVBOR") || val.startsWith("/9j/")); // PNG or JPG
    }

    private static boolean isTruthy(Object val) {
        if (val == null)
            return false;
        if (val instanceof Boolean b)
            return b;
        String s = val.toString().trim().toLowerCase();
        return s.equals("true") || s.equals("yes") || s.equals("1");
    }

    private static String getCheckboxOnValue(String[] states) {
        for (String state : states) {
            if (!state.equalsIgnoreCase("Off")) {
                return state;
            }
        }
        return "Yes";
    }
}