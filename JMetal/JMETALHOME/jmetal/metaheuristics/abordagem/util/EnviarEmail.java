package jmetal.metaheuristics.abordagem.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 *
 * @author Deyvid
 */
public class EnviarEmail {

    private EnviarEmail() {
    }

    public static void main(String[] args) throws NoSuchProviderException, MessagingException {
//        enviarRelatorioPorEmail("E://ulysses16_melhor_rota.txt", "");
        enviarEmail("Testando", "Deu zica ");
    }

    public static void enviarRelatorioPorEmail(String pathname, String mensagem) {
        try {
            Properties props = new Properties();
            /*Parâmetros de conexão com servidor Gmail*/

       

            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", "true");
//            props.put("mail.smtp.port", "465");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

            Session session = Session.getDefaultInstance(props,
                    new javax.mail.Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {

                            return new PasswordAuthentication("moraes.deyvid@gmail.com", "windowsmsn");
                        }
                    });

            /* Ativa Debug para sessão*/
            session.setDebug(true);

            //criando a mensagem
            MimeMessage message = new MimeMessage(session);

            //substituir pelos e-mails desejados
            Address from = new InternetAddress("moraes.deyvid@gmail.com");
//            Address to = new InternetAddress("moraes.deyvid@gmail.com");
            Address[] toUser = InternetAddress //Destinatário(s)
                    .parse("deyvid_moraes_200@hotmail.com, moraes.deyvid@gmail.com");
//                    .parse("deyvid_moraes_200@hotmail.com, cs_padre@hotmail.com, moraes.deyvid@gmail.com");

            //configurando o remetente e o destinatario
            message.setFrom(from);
            message.addRecipients(RecipientType.TO, toUser);

            //configurando a data de envio,  o assunto e o texto da mensagem
            message.setSentDate(new Date());
            message.setSubject("Relatório Experimentos dia " + formatarData());

            // conteudo html que sera atribuido a mensagem
            String htmlMessage = mensagem;

            //criando a Multipart
            Multipart multipart = new MimeMultipart();

            //criando a primeira parte da mensagem
            MimeBodyPart attachment0 = new MimeBodyPart();
            //configurando o htmlMessage com o mime type
            attachment0.setContent(htmlMessage, "text/html; charset=UTF-8");
            //adicionando na multipart
            multipart.addBodyPart(attachment0);

            //arquivo que será anexado
//            String pathname = "E://ulysses16_melhor_rota.txt";//pode conter o caminho
            File file = new File(pathname);

            //criando a segunda parte da mensagem
            MimeBodyPart attachment1 = new MimeBodyPart();
            //configurando o DataHandler para o arquivo desejado
            //a leitura dos bytes, descoberta e configuracao do tipo
            //do arquivo serão resolvidos pelo JAF (DataHandler e FileDataSource)
            attachment1.setDataHandler(new DataHandler(new FileDataSource(file)));
            //configurando o nome do arquivo que pode ser diferente do arquivo
            //original Ex: setFileName("outroNome.png")
            attachment1.setFileName(file.getName());
            //adicionando o anexo na multipart
            multipart.addBodyPart(attachment1);

            //adicionando a multipart como conteudo da mensagem
            message.setContent(multipart);

            //enviando
            Transport.send(message);

            System.out.println("E-mail enviado com sucesso!");
        } catch (AddressException ex) {
            Logger.getLogger(EnviarEmail.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MessagingException ex) {
            Logger.getLogger(EnviarEmail.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String formatarData() {
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date data = new Date();
        return fmt.format(data);
    }

    public static void enviarEmail(String assunto, String texto) throws RuntimeException {
        Properties props = new Properties();
        
//             props.setProperty("proxySet", "true");
//            props.setProperty("socksProxyHost", "proxyautenticado.utfpr.edu.br");
//            props.setProperty("socksProxyPort", "3128");
        
        /**
         * Parâmetros de conexão com servidor Gmail
         */
        props.put("mail.smtp.host", "smtp.gmail.com");
        //  props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.port", "465");
        
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {

                        return new PasswordAuthentication("moraes.deyvid@gmail.com", "windowsmsn");
                    }
                });

        /**
         * Ativa Debug para sessão
         */
        session.setDebug(true);

        try {

            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress("moraes.deyvid@gmail.com")); //Remetente

            Address[] toUser = InternetAddress //Destinatário(s)
                    //                    .parse("moraes.deyvid@gmail.com");
                    .parse("deyvid_moraes_200@hotmail.com, cs_padre@hotmail.com, moraes.deyvid@gmail.com");
//                             .parse("seuamigo@gmail.com, seucolega@hotmail.com, seuparente@yahoo.com.br");

            message.setRecipients(Message.RecipientType.TO, toUser);
            message.setSubject(assunto + " - " + formatarData());//Assunto
            message.setText(texto);
            /**
             * Método para enviar a mensagem criada
             */
            Transport.send(message);

            System.out.println("Feito!!!");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

}
