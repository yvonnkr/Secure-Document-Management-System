package com.yvolabs.securedocs.constant;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 13/06/2024
 */
public class SqlConstants {

    public static final String SELECT_COUNT_DOCUMENTS_QUERY = "SELECT COUNT(*) FROM documents";
    public static final String SELECT_DOCUMENTS_QUERY = """
            SELECT doc.id,
                   doc.document_id,
                   doc.name,
                   doc.description,
                   doc.uri,
                   doc.icon,
                   doc.size,
                   doc.formatted_size,
                   doc.extension,
                   doc.reference_id,
                   doc.created_at,
                   doc.updated_at,
                   CONCAT(owner.first_name, ' ', owner.last_name)     AS owner_name,
                   owner.email                                        AS owner_email,
                   owner.phone                                        AS owner_phone,
                   owner.last_login                                   AS owner_last_login,
                   CONCAT(updater.first_name, ' ', updater.last_name) AS updater_name
            FROM documents doc
                     JOIN users owner ON owner.id = doc.created_by
                     JOIN users updater ON updater.id = doc.updated_by""";


    public static final String SELECT_COUNT_DOCUMENTS_BY_NAME_QUERY = "SELECT COUNT(*) FROM documents WHERE name ~* :documentName";
    public static final String SELECT_DOCUMENTS_BY_NAME_QUERY = """
            SELECT doc.id,
                   doc.document_id,
                   doc.name,
                   doc.description,
                   doc.uri,
                   doc.icon,
                   doc.size,
                   doc.formatted_size,
                   doc.extension,
                   doc.reference_id,
                   doc.created_at,
                   doc.updated_at,
                   CONCAT(owner.first_name, ' ', owner.last_name)     AS owner_name,
                   owner.email                                        AS owner_email,
                   owner.phone                                        AS owner_phone,
                   owner.last_login                                   AS owner_last_login,
                   CONCAT(updater.first_name, ' ', updater.last_name) AS updater_name
            FROM documents doc
                     JOIN users owner ON owner.id = doc.created_by
                     JOIN users updater ON updater.id = doc.updated_by
            WHERE doc.name ~* :documentName""";


    public static final String SELECT_DOCUMENT_BY_DOCUMENT_ID_QUERY = """
            SELECT doc.id,
                   doc.document_id,
                   doc.name,
                   doc.description,
                   doc.uri,
                   doc.icon,
                   doc.size,
                   doc.formatted_size,
                   doc.extension,
                   doc.reference_id,
                   doc.created_at,
                   doc.updated_at,
                   CONCAT(owner.first_name, ' ', owner.last_name)     AS owner_name,
                   owner.email                                        AS owner_email,
                   owner.phone                                        AS owner_phone,
                   owner.last_login                                   AS owner_last_login,
                   CONCAT(updater.first_name, ' ', updater.last_name) AS updater_name
            FROM documents doc
                     JOIN users owner ON owner.id = doc.created_by
                     JOIN users updater ON updater.id = doc.updated_by
            WHERE doc.document_id= ?1""";
    //can use ?1 to indicate the 1st param

}
