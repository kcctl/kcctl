package org.moditect.kcctl.service.compaction;

import io.debezium.document.Array;
import io.debezium.document.Document;
import io.debezium.relational.history.JsonTableChangeSerializer;
import io.debezium.relational.history.TableChanges;

import java.util.Map;

public class HistoryRecord {

    public static final class Fields {
        public static final String SOURCE = "source";
        public static final String POSITION = "position";
        public static final String DATABASE_NAME = "databaseName";
        public static final String SCHEMA_NAME = "schemaName";
        public static final String DDL_STATEMENTS = "ddl";
        public static final String TABLE_CHANGES = "tableChanges";
    }

    private final Document doc;
    private static final TableChanges.TableChangesSerializer<Array> tableChangesSerializer = new JsonTableChangeSerializer();

    public HistoryRecord(Document document) {
        this.doc = document;
    }

    public HistoryRecord(Map<String, ?> source, Map<String, ?> position, String databaseName, String schemaName, String ddl, TableChanges changes) {
        this.doc = Document.create();

        Document src = doc.setDocument(HistoryRecord.Fields.SOURCE);
        if (source != null) {
            source.forEach(src::set);
        }

        Document pos = doc.setDocument(HistoryRecord.Fields.POSITION);
        if (position != null) {
            for (Map.Entry<String, ?> positionElement : position.entrySet()) {
                if (positionElement.getValue() instanceof byte[]) {
                    pos.setBinary(positionElement.getKey(), (byte[]) positionElement.getValue());
                }
                else {
                    pos.set(positionElement.getKey(), positionElement.getValue());
                }
            }
        }

        if (databaseName != null) {
            doc.setString(HistoryRecord.Fields.DATABASE_NAME, databaseName);
        }

        if (schemaName != null) {
            doc.setString(HistoryRecord.Fields.SCHEMA_NAME, schemaName);
        }

        if (ddl != null) {
            doc.setString(HistoryRecord.Fields.DDL_STATEMENTS, ddl);
        }

        if (changes != null) {
            doc.setArray(HistoryRecord.Fields.TABLE_CHANGES, tableChangesSerializer.serialize(changes));
        }

    }

    public Document document() {
        return this.doc;
    }

    protected Document source() {
        return doc.getDocument(HistoryRecord.Fields.SOURCE);
    }

    protected Document position() {
        return doc.getDocument(HistoryRecord.Fields.POSITION);
    }

    protected String databaseName() {
        return doc.getString(HistoryRecord.Fields.DATABASE_NAME);
    }

    protected String schemaName() {
        return doc.getString(HistoryRecord.Fields.SCHEMA_NAME);
    }

    protected String ddl() {
        return doc.getString(HistoryRecord.Fields.DDL_STATEMENTS);
    }

    protected Array tableChanges() {
        return doc.getArray(HistoryRecord.Fields.TABLE_CHANGES);
    }

    @Override
    public String toString() {
        return doc.toString();
    }

    /**
     * Verifies that the record contains mandatory fields - source and position
     *
     * @return false if mandatory fields are missing
     */
    public boolean isValid() {
        return source() != null && position() != null;
    }
}
