package fr.eni.jee.ggsencheres.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import fr.eni.jee.ggsencheres.bo.Article;
import fr.eni.jee.ggsencheres.bo.Enchere;
import fr.eni.jee.ggsencheres.bo.Utilisateur;

public class ArticleDAOJdbcImpl implements ArticleDAO {

	
	private static final String UPDATE_ENCHERE = "UPDATE ENCHERES SET montant_enchere=?,UTILISATEURS.pseudo=?,UTILISATEURS.credit=? "
			+ "INNER JOIN UTILISATEURS ON ENCHERES.no_utilisateur= UTILISATEURS.no_utilisateur "
			+ "WHERE ENCHERES.no_article = ?";


	private static final String SELECT_ARTICLE_BY_ID = "SELECT ARTICLES_VENDUS.no_article as no_article, nom_article, description, date_debut_enchere, date_fin_enchere, prix_initial, prix_vente, etat_vente, image, libelle, date_enchere, montant_enchere, CATEGORIES.no_categorie as no_categorie, CATEGORIES.libelle as libelle, UTILISATEURS.no_utilisateur as no_utilisateur, prenom, nom, pseudo, email, telephone, UTILISATEURS.rue as rue, UTILISATEURS.code_postal as code_postal, UTILISATEURS.ville as ville, mot_de_passe, credit, administrateur, RETRAITS.rue as retrue, RETRAITS.code_postal as retcode_postal, RETRAITS.ville as retville "
														+ "FROM ARTICLES_VENDUS "
														+ "LEFT OUTER JOIN ENCHERES ON ENCHERES.no_article = ARTICLES_VENDUS.no_article  "
														+ "INNER JOIN UTILISATEURS ON ARTICLES_VENDUS.no_utilisateur = UTILISATEURS.no_utilisateur "
														+ "INNER JOIN CATEGORIES ON ARTICLES_VENDUS.no_categorie = CATEGORIES.no_categorie "
														+ "INNER JOIN RETRAITS ON ARTICLES_VENDUS.no_article = RETRAITS.no_article  WHERE ARTICLES_VENDUS.no_article=?;";


	private static final String INSERT_INTO_RETRAITS = "INSERT INTO RETRAITS (no_article, rue, code_postal, ville) VALUES(?,?,?,?);";


	//Préciser les colonnes permet d'éviter la redondance de certaines informations notamment les clés étrangères(colonnes)
	private static final String SELECT_ENCHERES_EC = "SELECT ARTICLES_VENDUS.no_article as no_article, nom_article, description, date_debut_enchere, date_fin_enchere, prix_initial, prix_vente, etat_vente, image, libelle, date_enchere, montant_enchere, CATEGORIES.no_categorie as no_categorie, CATEGORIES.libelle as libelle, UTILISATEURS.no_utilisateur as no_utilisateur, prenom, nom, pseudo, email, telephone, UTILISATEURS.rue as rue, UTILISATEURS.code_postal as code_postal, UTILISATEURS.ville as ville, mot_de_passe, credit, administrateur, RETRAITS.rue as retrue, RETRAITS.code_postal as retcode_postal, RETRAITS.ville as retville "
													+ "FROM ARTICLES_VENDUS "
													+ "LEFT OUTER JOIN ENCHERES ON ENCHERES.no_article = ARTICLES_VENDUS.no_article "
													+ "INNER JOIN UTILISATEURS ON ARTICLES_VENDUS.no_utilisateur = UTILISATEURS.no_utilisateur "
													+ "INNER JOIN CATEGORIES ON ARTICLES_VENDUS.no_categorie = CATEGORIES.no_categorie "
													+ "INNER JOIN RETRAITS ON ARTICLES_VENDUS.no_article = RETRAITS.no_article "
													+ "WHERE ARTICLES_VENDUS.date_debut_enchere <= GETDATE() AND ARTICLES_VENDUS.date_fin_enchere > GETDATE();";
	
	private static final String SELECT_ENCHERES_CR = "SELECT ARTICLES_VENDUS.no_article, nom_article, description, date_debut_enchere, date_fin_enchere, prix_initial, prix_vente, etat_vente, image, libelle, date_enchere, montant_enchere, CATEGORIES.no_categorie, CATEGORIES.libelle, UTILISATEURS.no_utilisateur, prenom, nom, pseudo, email, telephone, UTILISATEURS.rue, UTILISATEURS.code_postal, UTILISATEURS.ville, UTILISATEURS.mot_de_passe, UTILISATEURS.credit, UTILISATEURS.administrateur, RETRAITS.rue, RETRAITS.code_postal, RETRAITS.ville "
													+ "FROM ARTICLES_VENDUS "
													+ "LEFT OUTER JOIN ENCHERES ON ENCHERES.no_article = ARTICLES_VENDUS.no_article "
													+ "INNER JOIN UTILISATEURS ON ARTICLES_VENDUS.no_utilisateur = UTILISATEURS.no_utilisateur "
													+ "INNER JOIN CATEGORIES ON ARTICLES_VENDUS.no_categorie = CATEGORIES.no_categorie "
													+ "INNER JOIN RETRAITS ON ARTICLES_VENDUS.no_article = RETRAITS.no_article "
													+ "WHERE ARTICLES_VENDUS.date_debut_enchere < GETDATE();";
	
	private static final String SELECT_ENCHERES_VD = "SELECT ARTICLES_VENDUS.no_article, nom_article, description, date_debut_enchere, date_fin_enchere, prix_initial, prix_vente, etat_vente, image, libelle, date_enchere, montant_enchere, CATEGORIES.no_categorie, CATEGORIES.libelle, UTILISATEURS.no_utilisateur, prenom, nom, pseudo, email, telephone, UTILISATEURS.rue, UTILISATEURS.code_postal, UTILISATEURS.ville, UTILISATEURS.mot_de_passe, UTILISATEURS.credit, UTILISATEURS.administrateur, RETRAITS.rue, RETRAITS.code_postal, RETRAITS.ville "
													+ "FROM ARTICLES_VENDUS "
													+ "LEFT OUTER JOIN ENCHERES ON ENCHERES.no_article = ARTICLES_VENDUS.no_article "
													+ "INNER JOIN UTILISATEURS ON ARTICLES_VENDUS.no_utilisateur = UTILISATEURS.no_utilisateur "
													+ "INNER JOIN CATEGORIES ON ARTICLES_VENDUS.no_categorie = CATEGORIES.no_categorie "
													+ "INNER JOIN RETRAITS ON ARTICLES_VENDUS.no_article = RETRAITS.no_article "
													+ "WHERE ARTICLES_VENDUS.date_fin_enchere < GETDATE();";



	private static final String INSERT_INTO_ARTICLES_VENDUS = "INSERT INTO ARTICLES_VENDUS (nom_article, description, date_debut_enchere, date_fin_enchere, prix_initial, prix_vente, no_utilisateur, no_categorie, etat_vente, image)VALUES(?,?,?,?,?,?,?,?,?,?);";


	@Override
	public void addArticle(Article articleAVendre) throws DALException {
		
		
		try (Connection cnx = ConnectionProvider.getConnection()){
		
			PreparedStatement pSt = cnx.prepareStatement(INSERT_INTO_ARTICLES_VENDUS,PreparedStatement.RETURN_GENERATED_KEYS);
			pSt.setString(1, articleAVendre.getNomArticle());
			pSt.setString(2, articleAVendre.getDescription());
			pSt.setTimestamp(3,  Timestamp.valueOf(articleAVendre.getDebutEnchere()));
			pSt.setTimestamp(4,  Timestamp.valueOf(articleAVendre.getFinEnchere()));
			pSt.setInt(5, articleAVendre.getPrixInitial());
			pSt.setInt(6,articleAVendre.getPrixVente());
			pSt.setInt(7,articleAVendre.getNoUtilisateur());
			pSt.setInt(8,articleAVendre.getNoCategorie());
			pSt.setString(9,"CR");
			pSt.setString(10,articleAVendre.getFichierPhotoArticle());
			
			pSt.executeUpdate();
			ResultSet rs = pSt.getGeneratedKeys();
			if (rs.next()) {
				articleAVendre.setNoArticle(rs.getInt(1));
			}
			
		}catch (SQLException e){
			throw new DALException("Erreur de connexion avec la base de données. Note technique : " + e.getMessage());
		}
		
		
	}
	public void addRetrait(Article articleAVendre) throws DALException {
		
		try(Connection cnx = ConnectionProvider.getConnection()){
			PreparedStatement pSt = cnx.prepareStatement(INSERT_INTO_RETRAITS);
			pSt.setInt(1, articleAVendre.getNoArticle());
			pSt.setString(2, articleAVendre.getRue());
			pSt.setString(3, articleAVendre.getCodePostal());
			pSt.setString(4, articleAVendre.getVille());
			


			pSt.executeUpdate();
		}catch(SQLException e) {
			throw new DALException("Erreur de connexion avec la base de données. Note technique : " + e.getMessage());
		}
		
	}

	@Override
	public List<Enchere> selectEncheresEC() throws DALException { //TODO AJOUTER LES PARAM JAVA dans la méthode
			Enchere enchereEC = null;
			List<Enchere> listeEncheres = new ArrayList<Enchere>();
			Utilisateur userEncherisseur = null;
			Article articleEC = null;
			
		
		try (Connection cnx = ConnectionProvider.getConnection()){
			PreparedStatement reqSelectEncheresEC = cnx.prepareStatement(SELECT_ENCHERES_EC);
			
			ResultSet rs = reqSelectEncheresEC.executeQuery();
			
			while (rs.next()) {
				
				// La table ENCHERES est une table de jointure entre les tables ARTICLES_VENDUS et UTILISATEURS,
				// Il en va de même pour la classe Enchere
				// Nous devons récupérer toutes les informations nécessaires à la création du constructeur de la classe Enchère en bo.
				
				
				// TABLE UTILISATEURS
					int noUtilisateur			= rs.getInt("no_utilisateur");
					String pseudo				= rs.getString("pseudo");
					String nom					= rs.getString("nom");
					String prenom				= rs.getString("prenom");
					String email				= rs.getString("email");
					String telephone			= rs.getString("telephone");
					String rue					= rs.getString("rue");
					String codePostal			= rs.getString("code_postal");
					String ville				= rs.getString("ville");
					String motDePasse			= rs.getString("mot_de_passe");
					int credit					= rs.getInt("credit");
					boolean administrateur		= rs.getBoolean("administrateur");
					System.out.println("noUtilisateur:" + noUtilisateur);
				// TABLE ARTICLES_VENDUS
					int noArticle 					= rs.getInt("no_article");
					String nomArticle				= rs.getString("nom_article");
				    String description				= rs.getString("description");
				    LocalDateTime dateDebutEnchere 	= LocalDateTime.of((rs.getDate("date_debut_enchere").toLocalDate()),rs.getTime("date_debut_enchere").toLocalTime()); //Le type DateTime (SQL) est converti en 2 variables: LocalDate et LocalTime
				    LocalDateTime dateFinEnchere 	= LocalDateTime.of((rs.getDate("date_fin_enchere").toLocalDate()),rs.getTime("date_fin_enchere").toLocalTime());
				    int prixInitial					= rs.getInt("prix_initial");
				    int prixVente					= rs.getInt("prix_vente");
				    String etatVente				= rs.getString("etat_vente");
				    String fichierPhotoArticle		= rs.getString("image");
				    System.out.println("noArticle:" + noArticle);
				 // TABLE CATEGORIES
				    int noCategorie					= rs.getInt("no_categorie");
				    String libelle					= rs.getString("libelle");
				    System.out.println("libelle:" + libelle);
				 // TABLE RETRAITS 
				    String rueRetrait				= rs.getString("retrue");
				    String codePostalRetrait		= rs.getString("retcode_postal");
				    String villeRetrait				= rs.getString("retville");
				    
				 // TABLE ENCHERES   
					int montantEnchere 				= rs.getInt("prix_initial"); //TODO Mettre le prix_initial? Si oui quelles conséquences pour les futures enchères?
					System.out.println("montant_enchere/Mise en ligne de l'article:" + montantEnchere);
					LocalDateTime dateEnchere 		= LocalDateTime.of((rs.getDate("date_debut_enchere").toLocalDate()),rs.getTime("date_debut_enchere").toLocalTime());
					System.out.println("dateEnchere/Mise en ligne de l'article:" + dateDebutEnchere);	
					
					userEncherisseur 	= new Utilisateur(noUtilisateur, nomArticle, prenom, pseudo, email, telephone, rue, codePostal, ville, motDePasse, credit, administrateur);
					System.out.println("....:" + userEncherisseur.getCodePostal());
					articleEC 			= new Article(noArticle, nomArticle, description, dateDebutEnchere, dateFinEnchere, prixInitial, prixVente, noUtilisateur, noCategorie, etatVente, fichierPhotoArticle, rueRetrait, codePostalRetrait, villeRetrait);
					
					enchereEC			= new Enchere(userEncherisseur, articleEC, dateEnchere, montantEnchere);
					System.out.println("....:" + enchereEC.getUserEncherisseur().getCodePostal());
					
					listeEncheres.add(enchereEC);
			}
			
		}catch(SQLException e) {			
			throw new DALException("Erreur de connexion avec la base de données. Note technique : " + e.getMessage());
		}
				return listeEncheres;
		
		
	}
	
	public Enchere selectArticleById(int noArticle) throws DALException {
		Article articleEC = null;
		Enchere enchereEC = null;
		Utilisateur userEncherisseur = null;
		
		try(Connection cnx = ConnectionProvider.getConnection()){
			PreparedStatement pSt = cnx.prepareStatement(SELECT_ARTICLE_BY_ID);
			
			pSt.setInt(1, noArticle);
			
			ResultSet rs = pSt.executeQuery();
			
			while (rs.next()) {
				// TABLE UTILISATEURS
				int noUtilisateur			= rs.getInt("no_utilisateur");
				String pseudo				= rs.getString("pseudo");
				String nom					= rs.getString("nom");
				String prenom				= rs.getString("prenom");
				String email				= rs.getString("email");
				String telephone			= rs.getString("telephone");
				String rue					= rs.getString("rue");
				String codePostal			= rs.getString("code_postal");
				String ville				= rs.getString("ville");
				String motDePasse			= rs.getString("mot_de_passe");
				int credit					= rs.getInt("credit");
				boolean administrateur		= rs.getBoolean("administrateur");
			//TABLE ARTICLES_VENDUS
				
				String nomArticle				= rs.getString("nom_article");
			    String description				= rs.getString("description");
			    LocalDateTime dateDebutEnchere 	= LocalDateTime.of((rs.getDate("date_debut_enchere").toLocalDate()),rs.getTime("date_debut_enchere").toLocalTime()); //Le type DateTime (SQL) est converti en 2 variables: LocalDate et LocalTime
			    LocalDateTime dateFinEnchere 	= LocalDateTime.of((rs.getDate("date_fin_enchere").toLocalDate()),rs.getTime("date_fin_enchere").toLocalTime());
			    int prixInitial					= rs.getInt("prix_initial");
			    int prixVente					= rs.getInt("prix_vente");
			    String etatVente				= rs.getString("etat_vente");
			    String fichierPhotoArticle		= rs.getString("image");
			    System.out.println("noArticle:" + noArticle);
			 // TABLE CATEGORIES
			    int noCategorie					= rs.getInt("no_categorie");
			    String libelle					= rs.getString("libelle");
			    System.out.println("libelle:" + libelle);
			 // TABLE RETRAITS 
			    String rueRetrait				= rs.getString("retrue");
			    String codePostalRetrait		= rs.getString("retcode_postal");
			    String villeRetrait				= rs.getString("retville");
			    // TABLE ENCHERES   
				int montantEnchere 				= rs.getInt("prix_initial"); //TODO Mettre le prix_initial? Si oui quelles conséquences pour les futures enchères?
				System.out.println("montant_enchere/Mise en ligne de l'article:" + montantEnchere);
				LocalDateTime dateEnchere 		= LocalDateTime.of((rs.getDate("date_debut_enchere").toLocalDate()),rs.getTime("date_debut_enchere").toLocalTime());
				System.out.println("dateEnchere/Mise en ligne de l'article:" + dateDebutEnchere);	
				
				userEncherisseur 	= new Utilisateur(noUtilisateur, nomArticle, prenom, pseudo, email, telephone, rue, codePostal, ville, motDePasse, credit, administrateur);
				System.out.println("....:" + userEncherisseur.getCodePostal());
				articleEC 			= new Article(noArticle, nomArticle, description, dateDebutEnchere, dateFinEnchere, prixInitial, prixVente, noUtilisateur, noCategorie, etatVente, fichierPhotoArticle, rueRetrait, codePostalRetrait, villeRetrait);
				
				enchereEC			= new Enchere(userEncherisseur, articleEC, dateEnchere, montantEnchere);
				System.out.println("....:" + enchereEC.getUserEncherisseur().getCodePostal());
				
				
			
			}
		}catch(SQLException e) {
			throw new DALException("");
		}
		return enchereEC;
	}
	
	public Enchere updateEnchereEC(int montantEnchere, int noArticle, String pseudoEncherisseur, int creditEncherisseur) throws DALException {
		Enchere enchereUpdated=null;
		try(Connection cnx = ConnectionProvider.getConnection()){
			PreparedStatement pSt= cnx.prepareStatement(UPDATE_ENCHERE);
			pSt.setInt(1, montantEnchere);
			pSt.setString(2, pseudoEncherisseur);
			pSt.setInt(3, creditEncherisseur);
			pSt.setInt(4, noArticle);
			pSt.executeUpdate();
		}catch(SQLException e) {
			throw new DALException("erreur de l'update de l'enchere");
		}
		return enchereUpdated;
	}
}
	
