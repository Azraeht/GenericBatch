import java.util.Properties;

import org.paris.batch.GenericBatch;
import org.paris.batch.config.ConfigurationManagerBatch;
import org.paris.batch.config.ConfigurationParameters;
import org.paris.batch.database.SQLExecutor;
import org.paris.batch.exception.CannotSendMailException;
import org.paris.batch.exception.CannotWriteTextToMessageException;
import org.paris.batch.exception.ConfigurationBatchException;
import org.paris.batch.exception.DatabaseDriverNotFoundException;
import org.paris.batch.exception.NoPropertiesFoundException;
import org.paris.batch.exception.SQLExecutorException;


public class TestBatch extends GenericBatch{

	public String sourcePath = "source";
	public String destinationPath = "destination";

	public TestBatch() throws ConfigurationBatchException,
	NoPropertiesFoundException {
		super();
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		TestBatch t;
		try {
			t = new TestBatch();
			t.run();
		} catch (ConfigurationBatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoPropertiesFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void setup() throws ConfigurationBatchException,
	DatabaseDriverNotFoundException {
		// TODO Auto-generated method stub

	}

	@Override
	public int run() {
		System.out.println("Test SQLExecutor");
		// Test No-commit SQLExecutor
		// Filtre les properties sur le suffixe correspondant à la connexion souhaité (Exemple : ".ff")
		// Le paramètre 'true' permet de supprimer le suffixe des clés des properties
		Properties p = ConfigurationManagerBatch.filterProperties(this.props, ".ff", true);

		// Initialisation du SQLExecutor avec comme argument les properties filtrées et le logger du batch
		SQLExecutor sqle;
		try {
			sqle = new SQLExecutor(p, this.logger);

			// Récupération de la requête à exécuter
			String requete = this.props.getProperty("query.sql.query01");

			//On exécute l'UPDATE sans paramètre
			//sqle.executeUpdate(requete);


			// Pour fermer la connexion
			sqle.close();
		} catch (DatabaseDriverNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ConfigurationBatchException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SQLExecutorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(this.props.getProperty(ConfigurationParameters.TEMPDIR));
		// Test No-commit Mailer
		System.out.println("Test Mailer");

		// Définition destinataire, emetteur et sujetc
		
		this.mailer.setFrom(this.props.getProperty("mail.sender"));
		this.mailer.setTo(this.props.getProperty("mail.dest"));
		this.mailer.setSubject(this.props.getProperty("mail.subject"));


		// Définition du texte
		try {
			this.mailer.setMainText(this.props.getProperty("mail.corps"));
		} catch (CannotWriteTextToMessageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			String id_mail = this.mailer.send(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			this.mailer.quickSend("brice.santus@paris.fr", "brice.santus@paris.fr", "Hello", "test", this.props);
		} catch (CannotSendMailException e) {
			// TODO Auto-generated catch block
			System.out.println(
					e.getLocalizedMessage());
		}

		return 0;
	}

	@Override
	public void finished() throws SQLExecutorException {
		System.out.println("Fin du traitement");

	}
}
