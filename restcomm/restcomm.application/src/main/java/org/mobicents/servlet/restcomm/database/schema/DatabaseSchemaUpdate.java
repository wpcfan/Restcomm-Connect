package org.mobicents.servlet.restcomm.database.schema;

import java.io.File;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.ibatis.migration.FileMigrationLoader;
import org.apache.ibatis.migration.JdbcConnectionProvider;
import org.apache.ibatis.migration.operations.UpOperation;
import org.apache.log4j.Logger;

public class DatabaseSchemaUpdate {

    private static final Logger logger = Logger
            .getLogger(DatabaseSchemaUpdate.class);
    private String jdbcDriver = null;
    private String jdbcUrl = null;
    private String jdbcUsername = null;
    private String jdbcPassword = null;
    private File migrationScriptsFile = null;
    private String currentDB = null;
    private String getDefaultDB ;






    public DatabaseSchemaUpdate(ServletContext context) throws Exception {

        String restcommXMLPath = context.getRealPath("WEB-INF/conf/restcomm.xml");
        String mybatisXMLPath = context.getRealPath("WEB-INF/conf/mybatis.xml");
        String dbDirectoryPath = context.getRealPath("WEB-INF/data/hsql");
        XMLConfiguration mybatisXML = new XMLConfiguration(mybatisXMLPath);
        XMLConfiguration restcommXML = new XMLConfiguration(restcommXMLPath);

        File hsqldbMigrationScriptsFiles = new File(
                restcommXML
                .getString("dao-manager.hsqldb-schema-update-scripts"));
        File mariadbMigrationScriptsFiles = new File(
                restcommXML
                .getString("dao-manager.mariadb-schema-update-scripts"));


        // this set method must run before creating changelogTable
        setDbConnectionDetails( mybatisXML, dbDirectoryPath);

        // Set directory to check for migration script files
        // production is for HSQLDB set in mybatis.xml
        if (currentDB.equalsIgnoreCase("production")) {
            migrationScriptsFile = hsqldbMigrationScriptsFiles;
        } else if (currentDB.equalsIgnoreCase("mariadb")) {
            migrationScriptsFile = mariadbMigrationScriptsFiles;

        }

        this.createChangeLogTable(currentDB, migrationScriptsFile);

    }




    // get JDBC connection info from my batis.xml
    @SuppressWarnings("unchecked")
    public void setDbConnectionDetails(XMLConfiguration mybatisXML, String dbDirectoryPath) {

        String dbDriver = null;
        String dbUrl = null;
        String dbUsername = null;
        String dbPassword = null;

        //get the current default DB from mybatix.xml
        getDefaultDB =  mybatisXML.getProperty("environments[@default]").toString() ;


        List<HierarchicalConfiguration> list = mybatisXML
                .configurationsAt("environments.environment");

        int counterDb = 0;
        for (HierarchicalConfiguration env : list) {

            logger.error("env.getString([@id]) " + env.getString("[@id]"));

            if (env.getString("[@id]").equalsIgnoreCase(getDefaultDB)){
                List<HierarchicalConfiguration> lists = mybatisXML
                        .configurationsAt("environments.environment("
                                + counterDb + ").dataSource.property");

                for (HierarchicalConfiguration property : lists) {
                        if(property.getString("[@name]").toString().equalsIgnoreCase("url")){
                            dbUrl = property.getString("[@value]").toString();
                            //expand the  ${data} path in mybatis.xml if DB is HSQLDB
                            if (getDefaultDB.equalsIgnoreCase("production")){

                                logger.error("value of Url  : " + dbUrl);
                                logger.error("value of dbDirectoryPath : " + dbDirectoryPath);

                                dbUrl = dbUrl.replace("/${data}", dbDirectoryPath);
                            logger.error("value of Url after replace : " + dbUrl);
                            }
                        }else if(property.getString("[@name]").toString().equalsIgnoreCase("username")){
                            dbUsername = property.getString("[@value]").toString();
                            logger.error("value of dbUsername : " + dbUsername);
                        }else if(property.getString("[@name]").toString().equalsIgnoreCase("password")){
                            dbPassword = property.getString("[@value]").toString();
                            logger.error("value of dbPassword : " + dbPassword);
                        }else if(property.getString("[@name]").toString().equalsIgnoreCase("driver")){
                            dbDriver = property.getString("[@value]").toString();
                            }

                        }

                    }

          counterDb++;
        }

            this.currentDB = getDefaultDB ;
            this.jdbcUrl = dbUrl;
            this.jdbcUsername = dbUsername;
            this.jdbcPassword = dbPassword;
            this.jdbcDriver = dbDriver;

    }



    public void createChangeLogTable(String currentDB, File migrationScriptsFile) throws Exception {

        //production is the default for HSQLDB
        if (currentDB.equalsIgnoreCase("production")) {
            try {
                new UpOperation().operate(new JdbcConnectionProvider(
                        jdbcDriver, jdbcUrl, jdbcUsername, jdbcPassword),
                        new FileMigrationLoader(migrationScriptsFile, null,
                                null), null, null);
            } catch (Exception e) {
                logger.error("Exception changelog table created", e);
            }
        }
        else if (currentDB.equalsIgnoreCase("mariadb")) {

            try {
                new UpOperation().operate(new JdbcConnectionProvider(
                        jdbcDriver, jdbcUrl, jdbcUsername, jdbcPassword),
                        new FileMigrationLoader(migrationScriptsFile, null,
                                null), null, null);
            } catch (Exception e) {
                logger.error("Exception when upgrading schema: " + e);
            }
        }
    }

}
