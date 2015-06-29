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

    public DatabaseSchemaUpdate(ServletContext context) throws Exception {

        String jbossStandaloneSipXMLPath = context.getRealPath("/");
        jbossStandaloneSipXMLPath = jbossStandaloneSipXMLPath.replace(
                "standalone/deployments/restcomm.war/",
                "standalone/configuration/standalone-sip.xml");
        String restcommXMLPath = context
                .getRealPath("WEB-INF/conf/restcomm.xml");
        String mybatisXMLPath = context.getRealPath("WEB-INF/conf/mybatis.xml");
        String dbDirectoryPath = context.getRealPath("WEB-INF/data/hsql");
        XMLConfiguration mybatisXML = new XMLConfiguration(mybatisXMLPath);
        XMLConfiguration restcommXML = new XMLConfiguration(restcommXMLPath);
        XMLConfiguration jbossStandaloneSipXML = new XMLConfiguration(
                jbossStandaloneSipXMLPath);

        File hsqldbMigrationScriptsFiles = new File(
                restcommXML
                .getString("dao-manager.hsqldb-schema-update-scripts"));
        File mariadbMigrationScriptsFiles = new File(
                restcommXML
                .getString("dao-manager.mariadb-schema-update-scripts"));

        //gets from mybatis.xml which DB to use

        // this set method must run before creating changelogTable
        setDbConnectionDetails(jbossStandaloneSipXML, mybatisXML,
                dbDirectoryPath);

        // Set directory to check for migration script files
        if (currentDB.equals("hsqldb")) {
            migrationScriptsFile = hsqldbMigrationScriptsFiles;
        } else if (currentDB.equals("mariadb")) {
            migrationScriptsFile = mariadbMigrationScriptsFiles;

        }

        this.createChangeLogTable(currentDB, migrationScriptsFile);

    }

    // get JDBC connection info from my batis.xml used for hsqldb
    @SuppressWarnings("unchecked")
    public void setDbConnectionDetails(XMLConfiguration jbossStandaloneSipXML,
            XMLConfiguration mybatisXML, String dbDirectoryPath) {

        String mariadbEnabledCon = null;
        String jdbcUrlCon = null;
        String jdbcUsernameCon = null;
        String jdbcPasswordCon = null;
        String jdbcDriverCon = null;

        // getting datasource and driver from standalone-sip.xml

        List<HierarchicalConfiguration> list = jbossStandaloneSipXML
                .configurationsAt("profile.subsystem");
        int counterSubsystem = 0;

        for (HierarchicalConfiguration sub : list) {

            if (sub.getString("[@xmlns]").contains(
                    "urn:jboss:domain:datasources")) {
                List<HierarchicalConfiguration> listDatasources = jbossStandaloneSipXML
                        .configurationsAt("profile.subsystem("
                                + counterSubsystem + ").datasources.datasource");
                List<HierarchicalConfiguration> listDrivers = jbossStandaloneSipXML
                        .configurationsAt("profile.subsystem("
                                + counterSubsystem
                                + ").datasources.drivers.driver");

                // get the datasource information
                for (HierarchicalConfiguration getListDatasources : listDatasources) {

                    if (getListDatasources.getString("[@enabled]").equals(
                            "true")) {
                        jdbcUrlCon = getListDatasources
                                .getString("connection-url");
                        jdbcUsernameCon = getListDatasources
                                .getString("security.user-name");
                        jdbcPasswordCon = getListDatasources
                                .getString("security.password");
                        mariadbEnabledCon = getListDatasources
                                .getString("[@enabled]");
                    }
                    // counterDatasource++;
                }
                // get the driver info
                for (HierarchicalConfiguration getlistDrivers : listDrivers) {
                    if (getlistDrivers.getString("[@name]").equals("mariadb")) {
                        jdbcDriverCon = getlistDrivers
                                .getString("xa-datasource-class");
                    }
                }
            }

            counterSubsystem++;
        }

        // set the DB to use to mariadb if the datasource in standalone-sip.xml
        // file is set to true
        if (jdbcUrlCon.contains("jdbc:mariadb".toLowerCase())
                && mariadbEnabledCon.equals("true")) {
            this.currentDB = "mariadb";
            this.jdbcUrl = jdbcUrlCon;
            this.jdbcUsername = jdbcUsernameCon;
            this.jdbcPassword = jdbcPasswordCon;
            this.jdbcDriver = jdbcDriverCon;
        } else { // use HSQLDB if maria db is not set
            String name = null;
            String value = null;
            List<HierarchicalConfiguration> lists = mybatisXML
                    .configurationsAt("environments.environment.dataSource.property");
            for (HierarchicalConfiguration sub : lists) {
                name = sub.getString("[@name]");
                value = sub.getString("[@value]");
                if (name.equals("driver")) {
                    jdbcDriverCon = value;
                } else if (name.equals("url")) {
                    // used to expand the ${data} from mybatix.xml
                    value = value.replace("${data}", dbDirectoryPath);
                    jdbcUrlCon = value;
                } else if (name.equals("username")) {
                    jdbcUsernameCon = value;
                } else if (name.equals("password")) {
                    jdbcPasswordCon = value;
                }
            }
            // set db to use to hsql
            this.currentDB = "hsqldb";
            this.jdbcUrl = jdbcUrlCon;
            this.jdbcUsername = jdbcUsernameCon;
            this.jdbcPassword = jdbcPasswordCon;
            this.jdbcDriver = jdbcDriverCon;
        }
    }



    public void createChangeLogTable(String currentDB, File migrationScriptsFile) throws Exception {

        if (currentDB.equals("hsqldb")) {
            try {
                new UpOperation().operate(new JdbcConnectionProvider(
                        jdbcDriver, jdbcUrl, jdbcUsername, jdbcPassword),
                        new FileMigrationLoader(migrationScriptsFile, null,
                                null), null, null);
            } catch (Exception e) {
                logger.error("Exception changelog table created", e);
            }
        }
        else if (currentDB.equals("mariadb")) {

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
