/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trustframework.evidence.github;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.xml.xpath.XPathExpressionException;
import trustframework.data.github.ProjectData;
import trustframework.evidence.EvidenceAnalyser;
import trustframework.exceptions.github.InvalidProjectData;
import trustframework.utils.Similarity;
import trustframework.graph.TFEdge;
import trustframework.graph.TFGraph;
import trustframework.utils.MapsAPI;

/**
 *
 * @author guilherme
 */
public class ProfileComunality implements EvidenceAnalyser {

    private double weigth;
    private static final String EVIDENCE_LABEL = "Similarity";
    private Properties properties;

    public ProfileComunality(double weigth) {
        this.weigth = weigth;
        InputStream propsFile = null;
        try {
            propsFile = ProfileComunality.class.getResourceAsStream("/resources/TF.properties");
            properties = new Properties();
            properties.load(propsFile);
            propsFile.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ProfileComunality.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ProfileComunality.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                propsFile.close();
            } catch (IOException ex) {
                Logger.getLogger(ProfileComunality.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public String getEvidenceLabel() {
        return EVIDENCE_LABEL;
    }

    @Override
    public void analyseEvidence(Object projectData, TFGraph relationsGraph) throws InvalidProjectData {
        if (projectData instanceof ProjectData) {
            ProjectData pData = (ProjectData) projectData;
            generateSimilarityEdges(relationsGraph, pData);
        } else {
            throw new InvalidProjectData("projectData is not an instance of " + ProjectData.class.getName());
        }
    }

    public void generateSimilarityEdges(TFGraph relationsGraph, ProjectData pData) {
        relationsGraph.allEdges("PR:").stream().collect(Collectors.groupingBy(TFEdge::getSource, Collectors.mapping(TFEdge::getTarget, Collectors.toSet()))).forEach((source, targets) -> {
            targets.forEach(target -> {
                generateSimilarityEdge(source, target, pData, relationsGraph);
            });
        });
    }

    public void generateSimilarityEdge(String source, String target, ProjectData pData, TFGraph relationsGraph) {
        double followSimilarity = Similarity.jaccardSimilarity(pData.getFollows(source), pData.getFollows(target));
        double watchSimilarity = Similarity.jaccardSimilarity(pData.getWatched(source), pData.getWatched(target));
        double locationSimilarity = getLocationSimilarity(pData.getUser(source).getLocation(), pData.getUser(target).getLocation());
        double profileSimilarity = (followSimilarity + watchSimilarity + locationSimilarity) / 3.0;
        relationsGraph.addEdge(source, target, EVIDENCE_LABEL, profileSimilarity, null);
        relationsGraph.addEdge(target, source, EVIDENCE_LABEL, profileSimilarity, null);
    }

    @Override
    public double getWeight() {
        return weigth;
    }

    @Override
    public void setWeight(double weight) {
        this.weigth = weight;
    }

    public double getLocationSimilarity(String location1, String location2) {
        Location loc1 = getLocationIndexes(location1);
        Location loc2 = getLocationIndexes(location2);
        return 1.0 / (1.0 + calculateDistance(loc1, loc2));
    }

    private Location getLocationIndexes(String location) {
        String key = "default";
        try {
            key = location != null ? MapsAPI.getCountry(location).replace(" ", "") : "default";
        } catch (IOException ex) {
            Logger.getLogger(ProfileComunality.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(ProfileComunality.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (properties.containsKey(key + ".pdi")) {
            return fillLocation(key);
        } else {
            return fillLocation("default");
        }
    }

    private double calculateDistance(Location loc1, Location loc2) {
        double idv = Math.pow(loc1.getIdv() - loc2.getIdv(), 2) / getPropertyValue("idv.var");
        double ivr = Math.pow(loc1.getIvr() - loc2.getIvr(), 2) / getPropertyValue("ivr.var");
        double lto = Math.pow(loc1.getLto() - loc2.getLto(), 2) / getPropertyValue("lto.var");
        double mas = Math.pow(loc1.getMas() - loc2.getMas(), 2) / getPropertyValue("mas.var");
        double pdi = Math.pow(loc1.getPdi() - loc2.getPdi(), 2) / getPropertyValue("pdi.var");
        double uai = Math.pow(loc1.getUai() - loc2.getUai(), 2) / getPropertyValue("uai.var");
        return Math.sqrt(idv + ivr + lto + mas + pdi + uai);
    }

    private Location fillLocation(String key) {
        Location loc = new Location();
        loc.setIdv(getPropertyValue(key + ".idv"));
        loc.setIvr(getPropertyValue(key + ".ivr"));
        loc.setLto(getPropertyValue(key + ".lto"));
        loc.setMas(getPropertyValue(key + ".mas"));
        loc.setPdi(getPropertyValue(key + ".pdi"));
        loc.setUai(getPropertyValue(key + ".uai"));
        return loc;
    }

    private double getPropertyValue(String property) {
        return Double.parseDouble(properties.getProperty(property));
    }

    @Override
    public void updateEvidence(Object projectData, TFGraph relations, Date limit, Date lastAnalysis) throws InvalidProjectData {
        if (projectData instanceof ProjectData) {
            ProjectData pData = (ProjectData) projectData;
            relations.allEdges(EVIDENCE_LABEL).stream().forEach(e -> {
                relations.removeEdge(e);
            });
            ProfileComunality.this.generateSimilarityEdges(relations, pData);
        } else {
            throw new InvalidProjectData("projectData is not an instance of " + ProjectData.class.getName());
        }
    }

}
