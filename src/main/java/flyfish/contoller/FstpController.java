//package flyfish.contoller;
//
//import lombok.extern.slf4j.Slf4j;
//import net.schmizz.sshj.SSHClient;
//import net.schmizz.sshj.sftp.SFTPClient;
//import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
//import net.schmizz.sshj.xfer.FileSystemFile;
//import net.schmizz.sshj.xfer.LocalSourceFile;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.io.IOException;
//
//@RestController
//@Slf4j
//public class FstpController {
//
//
//    @PostMapping("/tpi/uploadtry")
//    public void uploadFile(MultipartFile multipartFile, String serverIP, String username, String password, String remoteDirectory) throws IOException {
//        File tempFile = File.createTempFile("temp", multipartFile.getOriginalFilename());
//        multipartFile.transferTo(tempFile); // 将MultipartFile转换成File
//
//        SSHClient sshClient = new SSHClient();
//        sshClient.addHostKeyVerifier(new PromiscuousVerifier());
//
//        // Configure SSH client if necessary (e.g., add host key verifier)
//        try {
//            sshClient.connect(serverIP);
//            sshClient.authPassword(username, password);
//
//            try (SFTPClient sftpClient = sshClient.newSFTPClient()) {
//                LocalSourceFile localFile = new FileSystemFile(tempFile);
//                sftpClient.put(localFile, remoteDirectory);
//            } finally {
//                sshClient.disconnect();
//            }
//        } finally {
//            if (tempFile.exists()) {
//                tempFile.delete(); // 删除临时文件
//            }
//        }
//    }}