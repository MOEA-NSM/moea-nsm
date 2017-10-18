package jmetal.metaheuristics.moeansm.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author Deyvid
 * @date 10/02/2016
 */
public class UtilAP {

    public static String localRscript = "C:\\Program Files\\R\\R-3.2.3\\bin\\Rscript";

    public static String barra() {
        return System.getProperty("file.separator");
    }

    public static String barraDupla() {
        return System.getProperty("file.separator").concat(System.getProperty("file.separator"));
    }

    //@var arquivoExecutar = "E:\\jmetal\\AP_NSGAII_SPEA2\\R\\Problems_HV_Boxplot.R"
    public static void criarEPS(String arquivoExecutar) {
        BufferedReader reader = null;
        Process shell = null;
        try {
            shell = Runtime.getRuntime().exec(new String[]{localRscript, arquivoExecutar});
            reader = new BufferedReader(new InputStreamReader(shell.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int obterNumeroAleatorio(int minimo, int maximo) {
        try {
            Double valor = Math.random() * (maximo - minimo) + minimo;
            return valor.intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int calcularPorcentagem(double valor, double soma) {
        return (int) ((soma * valor) / 100);
    }

    public static String obterDiretorioProjeto() {
        String dir = System.getProperty("user.dir");
        String novaStr = "";
        for (int i = 0; i < dir.length(); i++) {
            if ((dir.charAt(i) == '\\') || (dir.charAt(i) == '/')) {
                novaStr += barraDupla();
            } else {
                novaStr += dir.charAt(i);
            }
        }
        return novaStr;
    }

    public static String obterDiretorioProjetoInstancias(String instancia) {
        return obterDiretorioProjeto() + barraDupla() + "instances" + barraDupla() + instancia + ".tsp";
    }

    public static String obterDiretorioProjetoParetoFront(String instancia) {
        return obterDiretorioProjeto() + barraDupla() + "paretoFront" + barraDupla() + instancia + ".pf";
    }

    public static void salvarValorArquivo(String pathArquivo, String valor) {
        FileWriter os;
        try {
            os = new FileWriter(pathArquivo, true);
            os.write("" + valor + "\n");
            os.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void resetFile(String file) {
        File f = new File(file);
        if (f.exists()) {
            System.out.println("File " + file + " exist.");

            if (f.isDirectory()) {
                System.out.println("File " + file + " is a directory. Deleting directory.");
                if (f.delete()) {
                    System.out.println("Directory successfully deleted.");
                } else {
                    System.out.println("Error deleting directory.");
                }
            } else {
                System.out.println("File " + file + " is a file. Deleting file.");
                if (f.delete()) {
                    System.out.println("File succesfully deleted.");
                } else {
                    System.out.println("Error deleting file.");
                }
            }
        } else {
            System.out.println("File " + file + " does NOT exist.");
        }
    } // resetFile

    public static long calcularMediaGeracaoTempo(String path) {
        File file = new File(path);
        FileInputStream arquivo;
        try {
            arquivo = new FileInputStream(file);
            InputStreamReader ler = new InputStreamReader(arquivo);
            BufferedReader x = new BufferedReader(ler);
            String linha = x.readLine();

            long geracaoTotal = 0;
            int totalItens = 0;
            while (linha != null) {
                totalItens++;
                geracaoTotal += Integer.parseInt(linha);
                linha = x.readLine();
            }

            return geracaoTotal / totalItens;
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            System.out.println("FALHA MEDIA GERACAO/TEMPO");
            return -100000;
        } catch (IOException ex) {
            ex.printStackTrace();
            return -100000;
        }
    }

    public static void main(String[] args) {
        System.out.println(obterNumeroAleatorio(0, 3));

    }

}
