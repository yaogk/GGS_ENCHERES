package fr.eni.jee.ggsencheres.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import fr.eni.jee.ggsencheres.bo.Article;
import fr.eni.jee.ggsencheres.bo.Enchere;

public class ArticleDAOJdbcImpl implements ArticleDAO {


	// private static final String SELECT_ALL_ENCHERE = "SELECT ";

	private static final String INSERT_INTO_ARTICLES_VENDUS = "INSERT INTO ARTICLES_VENDUS (nom_article, description, date_debut_enchere, date_fin_enchere, prix_initial, prix_vente, no_utilisateur, no_categorie, etat_vente, image)VALUES(?,?,?,?,?,?,?,?,?,?);";


	@Override
	public Article addArticle(Article articleAVendre) throws DALException {
		Article article=null;
		
		try (Connection cnx = ConnectionProvider.getConnection()){
		
			PreparedStatement pSt = cnx.prepareStatement(INSERT_INTO_ARTICLES_VENDUS);
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
			
			
		}catch (SQLException e){
			throw new DALException("Erreur de connexion avec la base de données. Note technique : " + e.getMessage());
		}
		
		return article;
	}

//	@Override
//	public List<Enchere> selectAll() throws DALException {
//		List<Enchere> listeEncheres = new ArrayList<>();
//		Enchere enchere;
		
//		try (Connection cnx = ConnectionProvider.getConnection()){
//			PreparedStatement pStmt = cnx.prepareStatement(SELECT_ALL_ENCHERE);
//		}
//		
//		return listeEncheres;
//	}

}
