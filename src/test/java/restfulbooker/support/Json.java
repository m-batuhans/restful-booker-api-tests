package restfulbooker.support;

import java.util.Map;

final class Json {

    private Json() {
    }

    static String toJson(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String string) {
            return "\"" + string.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        }
        if (value instanceof Map<?, ?> map) {
            StringBuilder json = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    json.append(",");
                }
                first = false;
                json.append("\"").append(entry.getKey()).append("\":").append(toJson(entry.getValue()));
            }
            return json.append("}").toString();
        }
        return String.valueOf(value);
    }
}
