
package org.paris.batch.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.log4j.Logger;
import org.paris.batch.config.ConfigurationManagerBatch;
import org.paris.batch.config.ConfigurationParameters;
import org.paris.batch.exception.ConfigurationBatchException;
import org.paris.batch.exception.DatabaseDriverNotFoundException;
import org.paris.batch.exception.SQLExecutorException;

/**
 * Services de base de données.
 * 
 * @author galloiem
 */
public class SQLExecutor {

    private Connection connection;
    private Logger logger;
    private QueryRunner runner;
    private ProcRunner procRunner;
    private Boolean nocommit;
    
    private Object preparedStatement;

    public static final String oracleDataTypeVarchar = "VARCHAR";
    public static final String oracleDataTypeVarchar2 = "VARCHAR2";
    public static final String oracleDataTypeNumber = "NUMBER";
    public static final String oracleDataTypeDate = "DATE";
    public static final String oracleDataTypeInteger = "INTEGER";
    public static final String oracleDataTypeBoolean = "BOOLEAN";
    
    private static final String listParamMark = "??";
    private static final String paramMark = "?";
    private static final String paramListSeparator = ",";
    
    private static final String queryKey = "query";
    private static final String paramsKey = "params";
    
    /**
     * Constructeur permettant d'exploiter l'indicateur no-commit
     * @param props Jeu de propriétés complet
     * @param propsFilter Chaîne à utiliser pour filtrer les propriétés nécessaires à l'élaboration de la connexion
     * @param keepRadix indicateur pour conserver ou non la chaîne filtrée dans les propriétés filtrées
     * @param logger logger à utiliser pour la trace du SQLExecutor
     * @throws DatabaseDriverNotFoundException si le chargement du driver échoue
     * @throws ConfigurationBatchException si un problème de configuration intervient
     */
    public SQLExecutor(Properties props, String propsFilter, boolean keepRadix, Logger logger)
        throws DatabaseDriverNotFoundException, ConfigurationBatchException {
        //instanciation
        super();
        
        //récupération du logger
        this.logger = logger;
        
        Properties filteredProps = ConfigurationManagerBatch.filterProperties(props, propsFilter, true);
        
        // Création de la connexion en fonction des propriétés transmises en paramètre
        this.logger.debug("Ouverture de la connexion en cours...");
        try
        {
        	this.connection = DBConnection.getConnection(filteredProps);
        }
        catch (DatabaseDriverNotFoundException ddnfe)
        {
        	logger.error("Pilote de base de données introuvable... y'a pas bon !\n" + ddnfe.getMessage());
        	throw ddnfe;
        }
        catch (Exception e)
        {
        	logger.error("Exception non prévue lors de la création de la connexion... ça fouetasse dans la purée !\n" + e.getMessage());
        	throw new ConfigurationBatchException(e.getMessage());
        }
        this.logger.debug("Connexion ouverte.");
        
        // Préparation du runner
        runner = new QueryRunner();
        
        // Mode no-commit
        if(props.getProperty(ConfigurationParameters.DB_NOCOMMIT_KEY).equals("true"))
        {
            this.logger.info("Mode no-commit détecté");
            this.nocommit = true;
        }
        else
        {
            this.nocommit = false;
        }
        this.logger.debug("SQLExecutor : Paramétrage");
        this.logger.debug("Connexion : "+
                        filteredProps.getProperty(ConfigurationParameters.DB_USER_KEY)+"/"+
                        filteredProps.getProperty(ConfigurationParameters.DB_PASS_KEY)+"@"+
                        filteredProps.getProperty(ConfigurationParameters.DB_HOST_KEY)+":"+
                        filteredProps.getProperty(ConfigurationParameters.DB_PORT_KEY));
        this.logger.debug("Mode Auto-Commit :"+filteredProps.getProperty(ConfigurationParameters.DB_AUTOCOMMIT_KEY));
        
    }

    /**
     * Constructeur
     * 
     * @param properties
     *            les informations nécessaires pour la création de la connection
     * @param logger
     *            journal d'événements
     * @throws DatabaseDriverNotFoundException
     * @throws ConfigurationBatchException
     */
    public SQLExecutor(Properties properties, Logger logger)
            throws DatabaseDriverNotFoundException, ConfigurationBatchException {
        super();
        
        
        //affecter le logger transmis en paramètre comme logger de cette classe
        this.logger = logger;

        
        // Création de la connexion en fonction des propriétés transmises en paramètre
        this.logger.debug("Ouverture de la connexion en cours...");
        this.connection = DBConnection.getConnection(properties);
        this.logger.debug("Connexion ouverte.");

        // Attribution du logger
        //this.logger = logger;
        
        // Préparation du runner
        runner = new QueryRunner();
        this.logger.debug("QueryRunner instancié");
        // préparation du procrunner
        procRunner = new ProcRunner();
        this.logger.debug("ProcRunner instancié");

        
        // Mode no-commit
        if(properties.getProperty(ConfigurationParameters.DB_NOCOMMIT_KEY) == null)
        {
        	String errMsg = "Une anomalie est survenue lors de l'instanciation du SQLExecutor :\n" 
        					+ "La property config.db.no-commit-mode(.ID) doit être renseignée dans le fichier de paramètrage";

            throw new ConfigurationBatchException(errMsg);
        }
        else if(properties.getProperty(ConfigurationParameters.DB_NOCOMMIT_KEY).equals("true"))
        {
            this.logger.info("Mode no-commit détecté");
            this.nocommit = true;
        }
        else
        {
        	this.logger.info("Mode commit détecté");
            this.nocommit = false;
        }
        
        this.logger.debug("SQLExecutor : Paramétrage");
        this.logger.debug("Connexion : "+properties.getProperty(ConfigurationParameters.DB_USER_KEY)+"/"+properties.getProperty(ConfigurationParameters.DB_PASS_KEY)+"@"+properties.getProperty(ConfigurationParameters.DB_HOST_KEY)+":"+properties.getProperty(ConfigurationParameters.DB_PORT_KEY));
        this.logger.debug("Mode Auto-Commit :"+properties.getProperty(ConfigurationParameters.DB_AUTOCOMMIT_KEY));
        
    }

    /**
     * Méthode fabriquant une requête précompilée (nécessaire pour le passage de paramètres complexes, type clause SELECT... WHERE x IN (?) )
     * @param query la requête à exécuter
     * @return la requête précompilée, sans paramètre spécifié
     * @throws SQLExecutorException
     */
    public void createPreparedStatement(String query) throws SQLExecutorException
    {
        String errMsg;
        try
        {
            if(DBConnection.isOracleConnection(connection))
            {
                //ps = connection.prepareStatement(query);
                preparedStatement = DBConnection.oraclePrepareStatement(connection, query);
            }
            else
                preparedStatement = connection.prepareStatement(query);
        }
        catch(SQLException sqle)
        {
            errMsg = "Une anomalie est survenue lors de la création de la requête précompilée :\n" + sqle.getMessage();
            this.logger.error(errMsg);
            throw new SQLExecutorException(errMsg);
        }
    }
    
    public void setArrayInPreparedStatement(int paramIndex, String dataType, Object[] paramArray) throws SQLExecutorException
    {
        String errMsg;
        try
        {
            if(DBConnection.isOracleConnection(connection))
            {
                DBConnection.oracleSetArray(connection, (PreparedStatement)preparedStatement, paramIndex, dataType, paramArray);
            }
            else
                ((PreparedStatement)preparedStatement).setArray(paramIndex, connection.createArrayOf(dataType, paramArray));
        }
        catch(SQLException sqle)
        {
            errMsg = "Une anomalie est survenue lors de l'ajout du tableau de paramètres à la requête précompilée :\n" + sqle.getMessage();
            this.logger.error(errMsg);
            throw new SQLExecutorException(errMsg);
        }
    }
    
    /**
     * @param handler
     * @param query
     *            la Requète SQL
     * @return une liste contenant les résultats
     * @throws SQLExecutorException
     */
    public List<?> executeSelectWithPreparedStatement(ResultSetHandler<List<Object[]>> handler) throws SQLExecutorException
    {
        @SuppressWarnings("rawtypes")
        List<?> result = new ArrayList();
        ResultSet res;
        try {
            logger.debug("Requète SQL : " + ((PreparedStatement)preparedStatement).toString());
            if(DBConnection.isOracleConnection(connection))
            {
                res = DBConnection.oraclePreparedStatementExecuteQuery((PreparedStatement)preparedStatement);
            }
            else
                res = ((PreparedStatement)preparedStatement).executeQuery();
            result = handler.handle(res);
            logger.debug("Requète exécutée. Eléments retournés : "
                    + result.size());
        } catch (Exception sqle) {
            String msg = "Exception à l'exécution de `" + ((PreparedStatement)preparedStatement).toString() + "`\n"
                    + sqle.getMessage();
            logger.error(msg);

            throw new SQLExecutorException(msg);
        }
        return result;
    }
    
    /**
     * Exécute une proc stock sans valeur de retour (pas de result set)
     * @param statementCall L'appel SQL d'exécution de la proc stock
     * @return true si la proc stock renvoie un result set (pas d'bol...), false si elle renvoie un compteur d'update ou si elle ne renvoie rien du tout (c'est déjà plus dans la cible)
     * @throws SQLExecutorException Si quelque chose a foiré dans la manoeuvre...
     */
    public boolean executeCallableStatement(String statementCall) throws SQLExecutorException
    {
        try
        {
            if(nocommit)
            {
                this.logger.info("Mode no-commit on : la proc stock ne sera pas exécutée");
                return false;
            }
            else
            {
                this.logger.info("Appel à exécuter : " + statementCall);
                return procRunner.executeProc(connection, statementCall);
            }
        }
        catch(SQLException sqle)
        {
            String msg = "Exception SQL à l'exécution de '" + statementCall + "'\n"
                    + sqle.getMessage();
            logger.error(msg);
            throw new SQLExecutorException(msg);
        }
        catch(Exception e)
        {
            String msg = "Exception imprévue à l'exécution de '" + statementCall +"'\n"
                    + e.getMessage();
            logger.error(msg);
            throw new SQLExecutorException(msg);
        }
    }
    
    /**
     * La méthode execute une procédure ou un package Oracle.
     * Elle s'appuie sur la classe ProcRunner qui vient compléter la librairie DBUtils
     * @throws SQLExecutorException
     */
    public List<?> executeCallableStatementWithResults(String statementCall) throws SQLExecutorException 
    {
        @SuppressWarnings("rawtypes")
        List<?> result = new ArrayList();
        try 
        {
        	// En cas de rollback
        	if(nocommit)
        	{
        		this.logger.info("Mode No-Commit On : Rollback effectué");
        		this.logger.info("Traitement non exécuté car rollback surement impossible.");
        	}
            logger.info("Appel à exécuter : " + statementCall);
            
            //result = runner.update(connection, query);
            result = procRunner.queryProc(connection, statementCall, new ArrayListHandler());
            logger.info("Traitement exécuté. Eléments retournés : " + result.size());
        } 
        catch (SQLException sqle) 
        {
            String msg = "Exception (sql) à l'exécution de '" + statementCall + "'\n"
                    + sqle.getMessage();
            logger.error(msg);
            throw new SQLExecutorException(msg);
        }
        catch (Exception e)
        {
            String msg = "Exception (pas sql) à l'exécution de '" + statementCall + "'\n" + e.getMessage();
            logger.error(msg);
            throw new SQLExecutorException(msg);
        }
        return result;        
    }
    
    /**
     * Ferme la connection ouverte par {@link #SQLExecutor(Properties, Logger)}
     * 
     * @throws SQLExecutorException
     */
    public void close() throws SQLExecutorException {
        try {
            logger.debug("Fermeture de la connection en cours...");
            this.connection.close();
            logger.debug("Fermeture de la connection effectuée.");

        } catch (Throwable t) {
            String msg = "Erreur à la fermeture de la connection : "
                    + t.getMessage();
            logger.error(msg);

            throw new SQLExecutorException(msg);
        }
    }

    /**
     * Méthode pour effectuer une Requète "DELETE","UPDATE" ou "INSERT" dans une
     * base de données sans paramétres.
     * 
     * @param query
     *            requete SQL à executer.
     * @return 1 si la Requète s'est executée ou 0 si elle ne s'est pas
     *         correctement exécutée.
     * @throws SQLExecutorException
     */
    public int executeUpdate(String query) throws SQLExecutorException
    {
    	logger.debug("executeUpdate - requête demandée : " + query);
        int result = 0;
        String uCaseQuery = query.toUpperCase();
        try {
        	// Cas de rollback impossible sur une commande SQL 
			if((nocommit) && (uCaseQuery.indexOf("ALTER") != -1 || uCaseQuery.indexOf("TRUNCATE") != -1 || uCaseQuery.indexOf("DROP") != -1 || uCaseQuery.indexOf("CREATE") != -1))
			{
				logger.info("Mode No-Commit On : requête non exécutée car Rollback impossible");
			}
			else
			{
	        	// Exécution de la requête
	            result = runner.update(connection, query);
	            logger.debug("Résultat de la requête : " + result);
	            
	            // Rollback si mode no-commit
	            if(nocommit)
	            {
	            	rollback();
	            	logger.info("Mode No-Commit On : rollback effectué");
	            }
	            else
	            {
	            	commit();
	            }
			}
            
        }
        catch(SQLException sqle)
        {
        	String msg = "SQLException à l'exécution de : " + query + "\n" + sqle.getMessage();
        	logger.error(msg);
        	throw new SQLExecutorException(msg);
        }
        catch (Exception e)
        {
            String msg = "Exception inattendue à l'exécution de : " + query + "\n" + e.getMessage();
            logger.error(msg);
            throw new SQLExecutorException(msg);
        }
        return result;
    }

    /**
     * Méthode pour effectuer une Requète "DELETE","UPDATE" ou "INSERT" dans une
     * base de données avec paramétres.
     * 
     * @param query
     *            la Requète SQL
     * @param params
     *            The query replacement parameters.
     * @return 1 si la Requète s'est executée ou 0 si elle ne s'est pas
     *         correctement exécutée.
     * @throws SQLExecutorException
     */
    public int executeUpdate(String query, Object... params)
            throws SQLExecutorException {
        int result = 0;
        
        HashMap<String, Object> infosAjustees;
        
        try {
        	// Cas de rollback impossible sur une commande SQL 
        	if((nocommit) && (query.indexOf("ALTER") != -1 || query.indexOf("TRUNCATE") != -1 || query.indexOf("DROP") != -1 || query.indexOf("CREATE") != -1)){
				logger.debug("Requête SQL : " + query);
				this.logger.info("Mode No-Commit On : Rollback effectué");
				logger.debug("Requête non exécutée car rollback impossible.");
			}else{
	        	// Exécution de la requête
	            logger.debug("Requète SQL : " + query);
	            for(Object arg:params){
	            	logger.info("Param : "+arg.toString());
	             }            
	            
	            //logger.debug("Appel à la fonction de substitution des paramètres multiples. Pour info :\n" + query + "\nNb de paramètres : " + params.length);
	            infosAjustees = adjustQueryForMultipleParams(query, params);
	            //logger.debug("Fonction de substitution des paramètres multiples effectuée. Pour info :\n" + (String) (infosAjustees.get(queryKey)) + "\nNb de paramètres : " + ((Object[]) (infosAjustees.get(paramsKey))).length);
	            
	            // org.apache.commons.dbutils.ResultSetHandler<T>
	            result = runner.update(connection, (String) (infosAjustees.get(queryKey)), (Object[]) (infosAjustees.get(paramsKey)));
	            
	            //result = runner.update(connection, query, params);
	            logger.debug("Requète exécutée. Résultat retourné : " + result);
	            
	            // Rollback si mode no-commit
	            if(nocommit){
	            	this.rollback();
	            	this.logger.info("Mode No-Commit On : Rollback effectué");
	            }
			}
        }
        catch (SQLException sqle)
        {
            String msg = "Exception à l'exécution de `" + query + "`\n"
                    + sqle.getMessage();
            logger.error(msg);

            throw new SQLExecutorException(msg);
        }
        return result;
    }

    /**
     * @param handler
     * @param query
     *            la Requète SQL
     * @return une liste contenant les résultats
     * @throws SQLExecutorException
     */
    public List<?> executeSelect(ResultSetHandler<List<Object[]>> handler,
            String query) throws SQLExecutorException {
        @SuppressWarnings("rawtypes")
        List<?> result = new ArrayList();
        try {
            logger.debug("Requète SQL : " + query);
            result = runner.query(connection, query, handler);
            logger.debug("Requète exécutée. Eléments retournés : "
                    + result.size());
        } catch (Exception sqle) {
            String msg = "Exception à l'exécution de `" + query + "`\n"
                    + sqle.getMessage();
            logger.error(msg);

            throw new SQLExecutorException(msg);
        }
        return result;
    }
    
      

    /**
     * @param query
     *            la Requète SQL
     * @param params
     *            The query replacement parameters.
     *            Each row in this array is one set of batch replacement values.
     * @return une liste contenant les résultats
     * @throws SQLExecutorException
     */
    public int[] callBatch(String query, Object[][] params)
           throws SQLExecutorException {
    	// TODO
    	int res[] = null;
        try {
        	
        	if(nocommit == false)
        		connection.setAutoCommit(true);
        	
            logger.debug("Requète SQL : " + query);
        	//Execute a batch of SQL INSERT, UPDATE, or DELETE queries. 
        	//The Connection is retrieved from the DataSource set in the constructor. 
        	//This Connection must be in auto-commit mode or the update will not be saved.
            res = runner.batch(connection,query,params);
            logger.debug("Batch exécuté. Nombre de déclarations traitées : "
                    + res.length);
            
            if(nocommit == false)
        		connection.setAutoCommit(false);
            
        } catch (SQLException sqle) {
            String msg = "Erreur lors de l'éxécution de la requête en mode \"batch\" `" + query + "`\n"
                    + sqle.getMessage();
            logger.error(msg);

            throw new SQLExecutorException(msg);
        }

    	return res;
    }
    
    
    
    

    public HashMap<String, Object> adjustQueryForMultipleParams(String query, Object...params)
    {
        Object[] remainingParams = new Object[params.length];
        Object[] paramList = {};
        Object param;
        String queryPart = "";
        int paramPos = query.indexOf(paramMark);
        int paramIndex = 0;
        int remainingParamIndex = 0;
        HashMap<String, Object> retour = new HashMap<String, Object>();
        logger.debug("ADJUST QUERY :\nquery :\n" + query + "\nparamPos : " + paramPos);
        while(paramPos >= 0)
        {
            
            if(paramPos == query.indexOf(listParamMark, paramPos))
            {
                logger.debug("substitution de double ??");
                //c'est une marque de liste : effectuer la substitution dans query, et supprimer le paramètre correspondant
                param = params[paramIndex];
                if(param instanceof Object[])
                {
                    paramList = (Object[]) param;
                    queryPart = "";
                    for(Object paramListItem:paramList)
                    {
                        //fabriquer la liste écrite des valeurs à transmettre
                        queryPart += paramListItem.toString() + paramListSeparator;
                    }
                    //remplacer la requête par 
                    query = query.substring(0, paramPos) + queryPart.substring(0,queryPart.length()-1) + (query.length() >= paramPos+2 ? query.substring(paramPos + 2):"");
                }
                else
                {
                    query = query.substring(0, paramPos) + param.toString() + (query.length() >= paramPos+2 ? query.substring(paramPos + 2):"");
                }
                paramIndex++;
            }
            else
            {
                //c'est un paramètre classique : on ne touche pas à la liste, on ajoute le paramètre
                //à la liste de ceux à conserver
                logger.debug("paramètre classique... on conserve.");
                remainingParams[remainingParamIndex] = params[paramIndex];
                remainingParamIndex++;
                paramIndex++;
            }
            paramPos = query.indexOf(paramMark, 
                                    ((paramPos+1>query.length())?query.length():paramPos+1)
                                    );
            logger.debug("prochaine position : " + paramPos);
        }
        paramList = new Object[] {};
        if(remainingParams[0] != null)
        {
            logger.debug("Constitution de la liste des paramètres allégée.");
            paramList = new Object[remainingParamIndex - 1];
            for(int i = 0; i < remainingParamIndex - 1; i++)
            {
                paramList[i] = remainingParams[i];
            }
            params = paramList;
        }

        logger.debug("Query :\n" + query);
        logger.debug("Nb params :\n" + paramList.length);
        retour.put(queryKey, new String(query) );
        retour.put(paramsKey, paramList);
        return(retour);
    }
    
    
    /**
     * @param query
     *            la Requète SQL
     * @param params
     * @return <code>List<Map<String, String>></code>
     * @throws SQLExecutorException
     */
    public List<?> executeSelect(String query, Object... params)
            throws SQLExecutorException {
        @SuppressWarnings("rawtypes")
        List<?> result = new ArrayList();
        HashMap<String, Object> infosAjustees;
        try {
            logger.debug("Requète SQL : " + query);
            for(Object arg:params){
            	logger.debug("Param : "+arg.toString());
             }
            
            //logger.debug("Appel à la fonction de substitution des paramètres multiples. Pour info :\n" + query + "\nNb de paramètres : " + params.length);
            infosAjustees = adjustQueryForMultipleParams(query, params);
            //logger.debug("Fonction de substitution des paramètres multiples effectuée. Pour info :\n" + (String) (infosAjustees.get(queryKey)) + "\nNb de paramètres : " + ((Object[]) (infosAjustees.get(paramsKey))).length);
            
            // org.apache.commons.dbutils.ResultSetHandler<T>
            result = runner.query(connection, (String) (infosAjustees.get(queryKey)), new MapListHandler(),
                    (Object[]) (infosAjustees.get(paramsKey)));
            logger.debug("Requète exécutée. Eléments retournés : "
                    + result.size());
        } catch (Exception sqle) {
            String msg = "Exception à l'exécution de `" + query + "`\n"
                    + sqle.getMessage();
            logger.error(msg);

            throw new SQLExecutorException(msg);
        }
        return result;
    }

    /**
     * @deprecated Wrapper pour la méthode
     *             <code>executeSelectWithRS(String query, Object... params)</code>
     * 
     * @param query
     *            la Requète SQL
     * @return <code>java.sql.ResultSet</code>.
     */
    @SuppressWarnings("javadoc")
    public ResultSet executeSelectWithRS(String query)
            throws SQLExecutorException {
        return executeSelectWithRS(query, (Object[]) null);
    }

    /**
     * @deprecated Attention, cette méthode a été écrite pour la transition du
     *             job MDPH0001. Il convient d'utiliser les autres méthodes qui
     *             ne retournent pas de <code>java.sql.ResultSet</code>.
     * 
     * @param query
     *            la Requète SQL
     * @return <code>java.sql.ResultSet</code>.
     */
    @SuppressWarnings("javadoc")
    public ResultSet executeSelectWithRS(String query, Object... params)
            throws SQLExecutorException {
        ResultSet result = null;
        try {
            logger.debug("Requète SQL : " + query);
            for(Object arg:params){
            	logger.debug("Param : "+arg.toString());
             }         
            PreparedStatement ps = connection.prepareStatement(query);
            if (params != null)
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            result = ps.executeQuery();
            logger.debug("Requète exécutée.");
        } catch (Exception sqle) {
            String msg = "Exception à l'exécution de `" + query + "`\n"
                    + sqle.getMessage();
            logger.error(msg);

            throw new SQLExecutorException(msg);
        }
        return result;
    }

    /**
     * @param handler
     *            comment traiter le ResultSet et alimenter le resultat.
     * @param query
     *            la Requète SQL
     * @param params
     *            liaison des paramétres pour la Requète SQL
     * @return une liste du type définie par le paramétre handler
     * @throws SQLExecutorException
     */
    public List<?> executeSelect(ResultSetHandler<List<Object[]>> handler,
            String query, Object... params) throws SQLExecutorException {
        @SuppressWarnings("rawtypes")
        List<?> result = new ArrayList();
        HashMap<String, Object> infosAjustees;
        try {
            logger.debug("Requète SQL : " + query);
            for(Object arg:params){
            	logger.debug("Param : "+arg.toString());
             }        
            // org.apache.commons.dbutils.ResultSetHandler<T>

            logger.debug("Appel à la fonction de substitution des paramètres multiples. Pour info :\n" + query + "\nNb de paramètres : " + params.length);
            infosAjustees = adjustQueryForMultipleParams(query, params);
            logger.debug("Fonction de substitution des paramètres multiples effectuée. Pour info :\n" + (String) (infosAjustees.get(queryKey)) + "\nNb de paramètres : " + ((Object[]) (infosAjustees.get(paramsKey))).length);
            
            result = runner.query(connection, (String) (infosAjustees.get(queryKey)), handler, (Object[]) (infosAjustees.get(paramsKey)));
            logger.debug("Requète exécutée. Eléments retournés : "
                    + result.size());
        } catch (Exception sqle) {
            String msg = "Exception à l'exécution de `" + query + "`\n"
                    + sqle.getMessage();
            logger.error(msg);

            throw new SQLExecutorException(msg);
        }
        return result;
    }
    /**
     * Méthode permettant d'envoyer un ordre de commit à la base
     */
    public void commit(){
    	try {
    		// Commit des changement
			this.connection.commit();
			this.logger.debug("Commit effectué");
		} catch (SQLException sqle) {
			String msg = "Exception lors du rollback : "+ sqle.getMessage();
            logger.error(msg);
		}
    }
    
    /**
     * Méthode permettant d'envoyer un ordre de rollback à la base
     */
    public void rollback(){
    	try {
    		// Rollback des changement
			this.connection.rollback();
			this.logger.debug("RollBack effectué");
		} catch (SQLException sqle) {
			String msg = "Exception lors du rollback : "+ sqle.getMessage();
            logger.error(msg);
		}
    }
    
    

}
