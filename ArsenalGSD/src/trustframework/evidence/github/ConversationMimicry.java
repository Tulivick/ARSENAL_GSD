/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trustframework.evidence.github;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.eclipse.egit.github.core.PullRequest;
import trustframework.data.github.GitComment;
import trustframework.data.github.ProjectData;
import trustframework.evidence.EvidenceAnalyser;
import trustframework.exceptions.github.InvalidProjectData;
import trustframework.utils.Similarity;
import trustframework.graph.TFEdge;
import trustframework.graph.TFGraph;

/**
 *
 * @author guilherme
 */
public class ConversationMimicry implements EvidenceAnalyser {

    private double weigth;
    private static final String EVIDENCE_LABEL = "Mimicry";

    public ConversationMimicry(double weigth) {
        this.weigth = weigth;
    }

    @Override
    public void analyseEvidence(Object projectData, TFGraph relationsGraph) throws InvalidProjectData {
        if (projectData instanceof ProjectData) {
            ProjectData pData = (ProjectData) projectData;
            generatePartialEdges(pData, relationsGraph);
            generateFinalEdges(pData, relationsGraph);
        } else {
            throw new InvalidProjectData("projectData is not an instance of " + ProjectData.class.getName());
        }
    }

    @Override
    public String getEvidenceLabel() {
        return EVIDENCE_LABEL;
    }

    @Override
    public double getWeight() {
        return weigth;
    }

    @Override
    public void setWeight(double weight) {
        this.weigth = weight;
    }

    public double cosineSimilarity(String s1, String s2) {
        try {
            if (s1.trim().equals("") || s2.trim().equals("")) {
                return 0.0;
            }
            Directory dir = indexTexts(s1, s2);
            IndexReader ir = DirectoryReader.open(dir);
            Map<String, Integer> f1 = getFrequencyMap(ir, 0);
            Map<String, Integer> f2 = getFrequencyMap(ir, 1);
            return Similarity.cosineSimilarity(f1, f2);
        } catch (IOException | NullPointerException ex) {
            if(!(ex instanceof NullPointerException)){
                Logger.getLogger(ConversationMimicry.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return 0.0;
    }

    private IndexWriter createIndexWriter(Directory dir) throws IOException {
        Analyzer analyser = new StandardAnalyzer(StandardAnalyzer.STOP_WORDS_SET);
        IndexWriterConfig iwc = new IndexWriterConfig(analyser);
        return new IndexWriter(dir, iwc);
    }

    private Document createDoc(String text) {
        Document doc = new Document();
        FieldType ft = new FieldType();
        ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        ft.setStored(true);
        ft.setStoreTermVectors(true);
        ft.setTokenized(true);
        Field cf1 = new Field("Content", text, ft);
        doc.add(cf1);
        return doc;
    }

    private Directory indexTexts(String text1, String text2) throws IOException {
        Directory dir = new RAMDirectory();
        IndexWriter iw = createIndexWriter(dir);
        iw.addDocument(createDoc(text1));
        iw.addDocument(createDoc(text2));
        iw.close();
        return dir;
    }

    private Map<String, Integer> getFrequencyMap(IndexReader ir, Integer docIndex) throws IOException {
        Terms frequencyVector = ir.getTermVector(docIndex, "Content");
        TermsEnum termsIterator = frequencyVector.iterator();
        Map<String, Integer> frequencyMap = new HashMap<>();
        BytesRef text;
        while ((text = termsIterator.next()) != null) {
            String term = text.utf8ToString();
            int freq = (int) termsIterator.totalTermFreq();
            frequencyMap.put(term, freq);
        }
        return frequencyMap;
    }

    private void generatePartialEdges(ProjectData pData, TFGraph relationsGraph) {
        generatePartialEdges(pData, relationsGraph, null);
    }

    private void generatePartialEdges(ProjectData pData, TFGraph relationsGraph, Date lastAnalysis) {
        pData.getAllComments().forEach((prId, prComments) -> {
            PullRequest pr = pData.getPullRequest(prId);
            //Collections.sort(prComments, new CommentsComparator()); é pra tar ordenado
            calculateSimilarityPR2Comments(pr, relationsGraph, prComments, lastAnalysis);
            calculateSimilarityBetweenComments(pr, relationsGraph, prComments, lastAnalysis);
        });
    }

    private void calculateSimilarityPR2Comments(PullRequest pr, TFGraph relationsGraph, List<GitComment> prComments, Date lastAnalysis) {
        prComments.stream().filter(c -> lastAnalysis == null || c.getUpdatedAt().after(lastAnalysis)).forEach((c) -> {
            double sim = cosineSimilarity(pr.getBody(), c.getBody());
            if (!pr.getUser().getLogin().equals(c.getCreator().getLogin())) {
                relationsGraph.addEdge(c.getCreator().getLogin(), pr.getUser().getLogin(), "P" + EVIDENCE_LABEL + ":" + pr.getNumber() + ":" + c.getId(), sim, pr.getCreatedAt());
            }
        });
    }

    private void calculateSimilarityBetweenComments(PullRequest pr, TFGraph relationsGraph, List<GitComment> prComments, Date lastAnalysis) {
        for (int i = 0; i < prComments.size(); i++) {
            for (int j = i + 1; j < prComments.size(); j++) {
                GitComment c1 = prComments.get(i);
                GitComment c2 = prComments.get(j);
                if (lastAnalysis == null || c2.getUpdatedAt().after(lastAnalysis)) {
                    double sim = cosineSimilarity(c1.getBody(), c2.getBody());
                    if (!c1.getCreator().getLogin().equals(c2.getCreator().getLogin())) {
                        relationsGraph.addEdge(c2.getCreator().getLogin(), c1.getCreator().getLogin(), "P" + EVIDENCE_LABEL + ":" + pr.getNumber() + ":" + c2.getId(), sim, c1.getUpdatedAt());
                    }
                }
            }
        }
    }

    private void generateFinalEdges(ProjectData pData, TFGraph relationsGraph) {
        pData.getInvolvedUsers().stream().forEach((user) -> {
            Map<String, List<TFEdge>> collect = relationsGraph.outEdges(user.getLogin(), "P" + EVIDENCE_LABEL).stream().collect(Collectors.groupingBy(TFEdge::getTarget));
            collect.forEach((targed, edges) -> {
                double mean = edges.stream().mapToDouble(TFEdge::getWeight).average().orElse(0);
                relationsGraph.addEdge(user.getLogin(), targed, EVIDENCE_LABEL, mean, null);
            });
        });
    }

    @Override
    public void updateEvidence(Object projectData, TFGraph relations, Date limit, Date lastAnalysis) throws InvalidProjectData {
        if (projectData instanceof ProjectData) {
            ProjectData pData = (ProjectData) projectData;
            removeOldEdges(relations, limit);
            generatePartialEdges(pData, relations, lastAnalysis);
            generateFinalEdges(pData, relations);
        } else {
            throw new InvalidProjectData("projectData is not an instance of " + ProjectData.class.getName());
        }
    }

    private void removeOldEdges(TFGraph relations, Date until) {
        relations.allEdges(EVIDENCE_LABEL).stream().forEach(e -> {
            relations.removeEdge(e);
        });
        relations.allEdges("P" + EVIDENCE_LABEL).stream().filter(e -> e.getDate().before(until)).forEach(e -> {
            relations.removeEdge(e);
        });
    }
}
