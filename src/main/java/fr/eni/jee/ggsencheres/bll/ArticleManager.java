package fr.eni.jee.ggsencheres.bll;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import fr.eni.jee.ggsencheres.bo.Article;
import fr.eni.jee.ggsencheres.bo.Enchere;
import fr.eni.jee.ggsencheres.bo.Utilisateur;
import fr.eni.jee.ggsencheres.dal.ArticleDAO;
import fr.eni.jee.ggsencheres.dal.DALException;
import fr.eni.jee.ggsencheres.dal.DAOFactory;


public class ArticleManager {
	private ArticleDAO articleDAO;
	private BLLException exceptions;
	
	
	
	public ArticleManager() {
		this.articleDAO = DAOFactory.getArticleDAO();
	}



	private void validerArticle(int noUtilisateur, String nomArticle, String description, int categorie,
			String fichierPhotoArticle, int prixInitial, LocalDateTime debutEnchere, LocalDateTime finEnchere,String rue, String codePostal, String ville)throws BLLException {
		exceptions= new BLLException();
		if(nomArticle == null || nomArticle.equalsIgnoreCase("")) {
			exceptions.addMessage("Erreur dans le nom de l'article");
		}
		if(description == null || description.equalsIgnoreCase("")) {
			exceptions.addMessage("Erreur dans la description de l'article");
		}
		if(categorie==0) {
			exceptions.addMessage("Erreur dans le numero de categorie de l'article");
		}
		if(debutEnchere == null && debutEnchere.compareTo(finEnchere)> 0  && debutEnchere.compareTo(LocalDateTime.now())<0) {
			exceptions.addMessage("Erreur dans le debut de l'enchere de l'article");
		}
		if(finEnchere == null) {
			exceptions.addMessage("Erreur dans la fin de l'enchere de l'article");
		}
		if(rue == null || rue.equalsIgnoreCase("")) {
			exceptions.addMessage("Erreur dans l'adresse de l'article");
		}
		if(codePostal == null || codePostal.equalsIgnoreCase("") || codePostal.length()!=5 || !codePostal.matches("^[0-9]+$")) {
			exceptions.addMessage("Erreur dans l'adresse de l'article");
		}
		if(ville == null || ville.equalsIgnoreCase("")) {
			exceptions.addMessage("Erreur dans l'adresse de l'article");
		}
		if(prixInitial < 0) {
			exceptions.addMessage("Vous devez fixer un prix superieur ou égal à 0");
		}
		if(!exceptions.isEmpty()) {
			throw exceptions;
		}
		
	}
	
	public Article creerAticleAVendre(int noUtilisateur, String nomArticle, String description, int categorie,
			String fichierPhotoArticle, int prixInitial, LocalDateTime debutEnchere, LocalDateTime finEnchere, String rue, String codePostal, String ville)throws BLLException {
		Article articleAVendre = new Article(noUtilisateur, nomArticle,description,categorie,fichierPhotoArticle,prixInitial,debutEnchere,finEnchere,rue,codePostal,ville);
		try {
			this.validerArticle(noUtilisateur, nomArticle, description, categorie, fichierPhotoArticle, prixInitial, debutEnchere, finEnchere, rue, codePostal, ville);
			
			articleDAO.addArticle(articleAVendre);
			articleDAO.addRetrait(articleAVendre);
		}catch(DALException e) {
			exceptions.addMessage(e.getMessage());
			throw exceptions;
		}
		
		return articleAVendre;
	}
	
	
	public List<Enchere> afficherEncheres() throws BLLException{
		
		List<Enchere> listeEncheres = new ArrayList<>();
		
		try {
			listeEncheres = this.articleDAO.selectEncheresEC();
			
		} catch (DALException e) {
			
			throw new BLLException("Erreur dans la méthode afficherEnchères. Note technique:" + e.getMessage());
		}
		return listeEncheres;
	}







	public Enchere selectArticleById(int noArticle) throws BLLException {
		Enchere enchereEC=null;
		try {
			enchereEC = this.articleDAO.selectArticleById(noArticle);
		} catch (DALException e) {
			e.printStackTrace();
			throw new BLLException("erreur dans la recuperation de l'article");
			
		}
		
		return enchereEC;
	}



	public Enchere validerEnchere(int montantEnchere, int noArticle, String pseudoEncherisseur, int creditEncherisseur) throws BLLException {
		Enchere enchereValidee=null;
		if(montantEnchere>creditEncherisseur) {
			throw new BLLException("credit insuffisant, désolé!");
		}
		try {
			enchereValidee=this.articleDAO.updateEnchereEC(montantEnchere,noArticle,pseudoEncherisseur,creditEncherisseur);
		} catch (DALException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return enchereValidee;
	}
}
