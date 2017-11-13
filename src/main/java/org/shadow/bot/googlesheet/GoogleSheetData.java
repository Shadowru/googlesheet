package org.shadow.bot.googlesheet;

public class GoogleSheetData implements IGoogleSheetData {
    private static final String DELIMITER = " : ";
    private String[] fields;

    public GoogleSheetData(String[] fields) {
        this.fields = fields;
    }

    @Override
    public String getSpecialMessage() {
        final StringBuilder stringBuilder = new StringBuilder();

        final String[] headers = GoogleSheetAdapter.getGoogleSheetAdapter().getHeader();

        for (int i = 1; i < headers.length; i++) {
            stringBuilder.append(headers[i]);
            stringBuilder.append(DELIMITER);
            try {
                stringBuilder.append(fields[i-1]);
                stringBuilder.append(" ");
            } catch (Exception e) {

            }
        }

        return stringBuilder.toString();
    }
}
