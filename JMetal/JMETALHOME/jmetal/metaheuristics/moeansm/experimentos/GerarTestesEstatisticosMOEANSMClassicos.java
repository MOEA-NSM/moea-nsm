package jmetal.metaheuristics.moeansm.experimentos;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jmetal.core.Algorithm;
import jmetal.experiments.Experiment;
import jmetal.experiments.Settings;
import jmetal.experiments.util.Friedman;
import jmetal.metaheuristics.moeansm.util.UtilAP;
import jmetal.util.JMException;

/**
 *
 * @author Deyvid
 * @date 25/04/2017
 */
public class GerarTestesEstatisticosMOEANSMClassicos extends Experiment {
 
    @Override
    public synchronized void algorithmSettings(String problemName, int problemIndex, Algorithm[] algorithm)
            throws ClassNotFoundException {
        try {
            int numberOfAlgorithms = algorithmNameList_.length;

            HashMap[] parameters = new HashMap[numberOfAlgorithms];

            for (int i = 0; i < numberOfAlgorithms; i++) {
                parameters[i] = new HashMap();
            }

            if (!paretoFrontFile_[problemIndex].equals("")) {
                for (int i = 0; i < numberOfAlgorithms; i++) {
                    parameters[i].put("paretoFrontFile_", paretoFrontFile_[problemIndex]);
                }
            }

            int maxGeracoes = 1000000;
            algorithm[0] = new MOEAD_NSM_Configuracoes(problemName, maxGeracoes).configure(parameters[0]);
//            algorithm[1] = new MOEAD_N_Configuracoes(problemName, maxGeracoes).configure(parameters[1]);
//            algorithm[2] = new MOEAD_S_Configuracoes(problemName, maxGeracoes).configure(parameters[2]);
//            algorithm[3] = new MOEAD_M_Configuracoes(problemName, maxGeracoes).configure(parameters[3]);
 
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(GerarTestesEstatisticosMOEANSMClassicos.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JMException ex) {
            Logger.getLogger(GerarTestesEstatisticosMOEANSMClassicos.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(GerarTestesEstatisticosMOEANSMClassicos.class.getName()).log(Level.SEVERE, null, ex);
        }
    }  

    public static void main(String[] args) throws JMException, IOException {
        executar();
    }  

    public static void executar() throws JMException, IOException {
        GerarTestesEstatisticosMOEANSMClassicos exp = new GerarTestesEstatisticosMOEANSMClassicos();
        
        //NAO USAR TRAÇO NO NOME DOS ALGORITMOS nem UNDERLINE
        //NAO USAR TRAÇO NO NOME DOS ALGORITMOS nem UNDERLINE
        //NAO USAR TRAÇO NO NOME DOS ALGORITMOS nem UNDERLINE
        
        exp.experimentName_ = "EXPERIMENTONSM11052017";
//        exp.algorithmNameList_ = new String[]{"MOEANSM"};
        exp.algorithmNameList_ = new String[]{"SPEA2", "NSGAII", "MOEAD", "ABNSGAIISPEA2MOEADBL"};
        exp.problemList_ = new String[]{
            "TSPMMEuclidAB100", "TSPMMEuclidAB300", "TSPMMEuclidAB500",
            "TSPMMEuclidCD100", "TSPMMEuclidCD300", "TSPMMEuclidCD500",
            "TSPMMEuclidEF100", "TSPMMEuclidEF300", "TSPMMEuclidEF500"};
 
 
        exp.paretoFrontFile_ = new String[]{
            "besteuclidAB100.pf", "besteuclidAB300.pf", "besteuclidAB500.pf",
            "besteuclidCD100.pf", "besteuclidCD300.pf", "besteuclidCD500.pf",
            "besteuclidEF100.pf", "besteuclidEF300.pf", "besteuclidEF500.pf"};
 
        exp.indicatorList_ = new String[]{"HV", "EPSILON", "R2"};

        int numberOfAlgorithms = exp.algorithmNameList_.length;

        exp.experimentBaseDirectory_ = "C:\\Users\\Deyvid\\Dropbox\\Dados Revista IEEE\\";
//        exp.experimentBaseDirectory_ = UtilAP.obterDiretorioProjeto() + UtilAP.barraDupla() + "experimentos" + UtilAP.barraDupla() + exp.experimentName_;
//        exp.paretoFrontDirectory_ = UtilAP.obterDiretorioProjeto() + UtilAP.barra() + "paretoFront";

        exp.algorithmSettings_ = new Settings[numberOfAlgorithms];

//        exp.independentRuns_ = 30;

        exp.initExperiment();

        // Run the experiments
//        int numberOfThreads;
//        exp.runExperiment(numberOfThreads = 1);

//        exp.generateQualityIndicators();

        // Generate latex tables (comment this sentence is not desired)
        exp.generateLatexTables();

        // Configure the R scripts to be generated
//        int rows;
//        int columns;
//        String prefix;
//        String[] problems;
//
//        rows = 3;
//        columns = 1;
//        prefix = new String("Problema");
//        problems = new String[]{
//            "EuclidAB100", "EuclidAB300", "EuclidAB500",
//            "EuclidCD100", "EuclidCD300", "EuclidCD500",
//            "EuclidEF100", "EuclidEF300", "EuclidEFB500"};
//
//        boolean notch;
//        
////        exp.generateRBoxplotScripts(rows, columns, problems, prefix, notch = true, exp);
//        exp.generateRWilcoxonScripts(problems, prefix, exp);

//        exp.generateReferenceFronts();

        // Applying Friedman test
        Friedman test = new Friedman(exp);
        test.executeTest("EPSILON");
        test.executeTest("HV");
        test.executeTest("R2"); 

        System.out.println("Fim");
//        String dirZip = exp.experimentBaseDirectory_ + UtilAP.barraDupla() + exp.experimentName_ + "zip";
//        try {
//            ZipUtils.compress(new File(exp.experimentBaseDirectory_), new File(dirZip));
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }

//        System.exit(0);
//        EnviarEmail.enviarRelatorioPorEmail(dirZip, "Resultado dos experimentos, 9 datasets, 10 abordagens 24/11/2016");
    }
} // Teste1

