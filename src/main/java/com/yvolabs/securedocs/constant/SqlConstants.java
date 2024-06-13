package com.yvolabs.securedocs.constant;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 13/06/2024
 */
public class SqlConstants {

    public static final String SELECT_COUNT_DOCUMENTS_QUERY = "SELECT COUNT(*) FROM documents";
    public static final String SELECT_DOCUMENTS_QUERY = "SELECT doc.id,\n" +
            "       doc.document_id,\n" +
            "       doc.name,\n" +
            "       doc.description,\n" +
            "       doc.uri,\n" +
            "       doc.icon,\n" +
            "       doc.size,\n" +
            "       doc.formatted_size,\n" +
            "       doc.extension,\n" +
            "       doc.reference_id,\n" +
            "       doc.created_at,\n" +
            "       doc.updated_at,\n" +
            "       CONCAT(owner.first_name, ' ', owner.last_name)     AS owner_name,\n" +
            "       owner.email                                        AS owner_email,\n" +
            "       owner.phone                                        AS owner_phone,\n" +
            "       owner.last_login                                   AS owner_last_login,\n" +
            "       CONCAT(updater.first_name, ' ', updater.last_name) AS updater_name\n" +
            "FROM documents doc\n" +
            "         JOIN users owner ON owner.id = doc.created_by\n" +
            "         JOIN users updater ON updater.id = doc.updated_by";
}
