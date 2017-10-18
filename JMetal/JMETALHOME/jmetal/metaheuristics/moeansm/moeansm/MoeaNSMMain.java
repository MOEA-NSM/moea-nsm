package jmetal.metaheuristics.moeansm.moeansm;

import jmetal.metaheuristics.moeansm.util.UtilAP;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.SelectionFactory;
import jmetal.problems.TSPMM.TSPMMEuclidAB100;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.Configuration;
import jmetal.util.JMException;

/**
 *
 * @author Deyvid
 * @date 26/01/2016
 */
public class MoeaNSMMain {

    public static Logger logger_;
    public static FileHandler fileHandler_;

    public static void main(String[] args) throws JMException, SecurityException, IOException, ClassNotFoundException {

        Problem problema = null;    // O problema a ser resolvido
        Algorithm algoritmo = null; // O algoritmo usado
        Operator crossover = null;  // Operador de cruzamento
        Operator mutacao = null;    // Operador de mutacao
        Operator buscaLocal = null; // Operador de busca local
        Operator selecao = null;    // Operador de selecao

        HashMap parametros; // Parametros de configuracao passados para o algoritmo

        QualityIndicator indicadores = null; // Indicadores de qualidade

        logger_ = Configuration.logger_;
        fileHandler_ = new FileHandler("MoeaNSMMain.log");
        logger_.addHandler(fileHandler_);

        problema = new TSPMMEuclidAB100("");
        indicadores = new QualityIndicator(problema, UtilAP.obterDiretorioProjetoParetoFront("besteuclidAB100"));
        algoritmo = new MoeaNSM(problema);

        int populacaoTotal = 100;
        algoritmo.setInputParameter("populacaoQtdPontas", UtilAP.calcularPorcentagem(30, populacaoTotal));
        algoritmo.setInputParameter("populacaoQtdPonderada", UtilAP.calcularPorcentagem(40, populacaoTotal));
        algoritmo.setInputParameter("populacaoQtdNSGAII", UtilAP.calcularPorcentagem(60, populacaoTotal));
        algoritmo.setInputParameter("populacaoQtdSPEA2", UtilAP.calcularPorcentagem(60, populacaoTotal));
        algoritmo.setInputParameter("populacaoTchebyCheff", UtilAP.calcularPorcentagem(100, populacaoTotal));
        algoritmo.setInputParameter("maxGeracoes", 2000000);

        parametros = new HashMap();

        parametros.put("probability", 0.80);
        crossover = CrossoverFactory.getCrossoverOperator("OXCrossover", parametros);

        parametros = new HashMap();
        parametros.put("probability", 0.1);
        mutacao = MutationFactory.getMutationOperator("SwapMutation", parametros);

        parametros = new HashMap();
        parametros.put("probability", 0.1);
        parametros.put("problem", problema);
        buscaLocal = MutationFactory.getMutationOperator("DoisOpt", parametros);

        parametros = null;
        selecao = SelectionFactory.getSelectionOperator("BinaryTournament", parametros);

        /*Adiciona os operadores ao algoritmo*/
        algoritmo.addOperator("crossover", crossover);
        algoritmo.addOperator("mutacao", mutacao);
        algoritmo.addOperator("buscaLocal2OPT", buscaLocal);
        algoritmo.addOperator("selecao", selecao);

        algoritmo.setInputParameter("indicators", indicadores);

        long initTime = System.currentTimeMillis();
        SolutionSet populacao = algoritmo.execute();
        long tempo = System.currentTimeMillis() - initTime;

        /*Mensagens de resultados*/
        logger_.info("Tempo de execucao: " + tempo + " ms");
        logger_.info("Valores das variaveis escritos em AbordagemProposta_variaveis");
        populacao.printVariablesToFile("AbordagemProposta_variaveis");
        logger_.info("Valores dos objetivos escritos em AbordagemProposta_objetivos");
        populacao.printObjectivesToFile("AbordagemProposta_objetivos");

        if (indicadores != null) {
            logger_.info("Indicadores de Qualidade");
            logger_.info("Hypervolume: " + indicadores.getHypervolume(populacao));
            logger_.info("GD         : " + indicadores.getGD(populacao));
            logger_.info("IGD        : " + indicadores.getIGD(populacao));
            logger_.info("Spread     : " + indicadores.getSpread(populacao));
            logger_.info("Epsilon    : " + indicadores.getEpsilon(populacao));
            int avaliacoes = ((Integer) algoritmo.getOutputParameter("requiredEvaluations"));
            logger_.info("requiredEvaluations HV      : " + avaliacoes + " avaliacoes");
        }
    }
}
