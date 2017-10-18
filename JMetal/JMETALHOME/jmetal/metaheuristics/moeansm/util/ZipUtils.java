package jmetal.metaheuristics.moeansm.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Classe utilitária para compactação e descompactação de arquivos ZIP
 *
 * @author Ricardo Artur Staroski
 */
public final class ZipUtils {

    public static void main(String[] args) {

        try {
            compress(new File("C:\\teste"), new File("E:\\teste.zip"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Compacta determindado arquivo ou diretório para o arquivo ZIP
     * especificado
     *
     * @param input O arquivo ou diretório de entrada
     * @param output O arquivo ZIP de saída
     */
    public static void compress(final File input, final File output) throws IOException {
        if (!input.exists()) {
            throw new IOException(input.getName() + " não existe!");
        }
        if (output.exists()) {
            if (output.isDirectory()) {
                throw new IllegalArgumentException("\"" + output.getAbsolutePath() + "\" não é um arquivo!");
            }
        } else {
            final File parent = output.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            output.createNewFile();
        }
        final ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(output));
        zip.setLevel(Deflater.BEST_COMPRESSION);
        compress("", input, zip);
        zip.finish();
        zip.flush();
        zip.close();
    }

    /**
     * Extrai um arquivo ZIP para o diretório especificado
     *
     * @param input O arquivo ZIP de entrada
     * @param output O diretório de saída
     */
    public static void extract(final File input, final File output) throws IOException {
        if (input.exists()) {
            if (input.isDirectory()) {
                throw new IllegalArgumentException("\"" + input.getAbsolutePath() + "\" não é um arquivo!");
            }
        } else {
            throw new IllegalArgumentException("\"" + input.getAbsolutePath() + "\" não existe!");
        }
        if (output.exists()) {
            if (output.isFile()) {
                throw new IllegalArgumentException("\"" + output.getAbsolutePath() + "\" não é um diretório!");
            }
        }
        final ZipFile zip = new ZipFile(input);
        extract(zip, output);
        zip.close();
    }

    // Adiciona determinado arquivo ao ZIP
    private static void compress(final String caminho, final File arquivo, final ZipOutputStream saida) throws IOException {
        final boolean dir = arquivo.isDirectory();
        final String nome = arquivo.getName();
        final ZipEntry elemento = new ZipEntry(caminho + '/' + nome + (dir ? "/" : ""));
        elemento.setSize(arquivo.length());
        elemento.setTime(arquivo.lastModified());
        saida.putNextEntry(elemento);
        if (dir) {
            final File[] arquivos = arquivo.listFiles();
            for (int i = 0; i < arquivos.length; i++) {
                // recursivamente adiciona outro arquivo ao ZIP
                compress(caminho + '/' + nome, arquivos[i], saida);
            }
        } else {
            final FileInputStream entrada = new FileInputStream(arquivo);
            copy(entrada, saida);
            entrada.close();
        }
    }

    // Copia o conteúdo do stream de entrada para o stream de saída
    private static void copy(final InputStream in, final OutputStream out) throws IOException {
        final int n = 4096;
        final byte[] b = new byte[n];
        for (int r = -1; (r = in.read(b, 0, n)) != -1; out.write(b, 0, r)) {
        }
        out.flush();
    }

    // Retira determinado elemento do arquivo ZIP
    private static void extract(final ZipFile zip, final File pasta) throws IOException {
        InputStream entrada = null;
        OutputStream saida = null;
        String nome = null;
        File arquivo = null;
        ZipEntry elemento = null;
        final Enumeration<?> elementos = zip.entries();
        while (elementos.hasMoreElements()) {
            elemento = (ZipEntry) elementos.nextElement();
            nome = elemento.getName();
            nome = nome.replace('/', File.separatorChar);
            nome = nome.replace('\\', File.separatorChar);
            arquivo = new File(pasta, nome);
            if (elemento.isDirectory()) {
                arquivo.mkdirs();
            } else {
                if (!arquivo.exists()) {
                    final File parent = arquivo.getParentFile();
                    if (parent != null) {
                        parent.mkdirs();
                    }
                    arquivo.createNewFile();
                }
                saida = new FileOutputStream(arquivo);
                entrada = zip.getInputStream(elemento);
                copy(entrada, saida);
                saida.flush();
                saida.close();
                entrada.close();
            }
            arquivo.setLastModified(elemento.getTime());
        }
    }

    // Construtor privado - Náo há razão em instanciar esta classe
    private ZipUtils() {
    }
}
