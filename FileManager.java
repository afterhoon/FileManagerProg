/***************************************************************


   1. 이미지가 많을 경우 불러오는 시간으로 인해 오래걸림
   2. 실행하고자 하는 파일의 상위 디렉토리 및 파일명에는 띄어쓰기가 없어야 함
   3. 각종 루트 다 지정 필요함
   4. 파일 순서 변경 불가, list로 보기 불가, 검색불가, 그룹화 불가


 ****************************************************************/

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.mortennobel.imagescaling.*;

public class FileManager extends JFrame {

   private JPanel contentPane;
   JPanel info_panel;
   private JTextField textField;
   Vector<FileInfo> vFile;
   Vector<FileInfo> vImage;
   Vector<FileInfo> vDocum;
   Vector<FileInfo> vAudio;
   Vector<FileInfo> vEtc;
   Vector<FileInfo> vCurr;
   Vector<FileInfo> vSearch;
   Vector<MediaPanel> vFilePan;
   Vector<JLabel> vIfLab;
   FileInfo targetFile;
   JList list;

   JTextField pageLa;
   JPanel viewPanel;
   JLabel pageLa2;

   //   int viewWid = 3;
   //   int viewHei = 4;
   int viewWid = 5;
   int viewHei = 3;
   int viewCnt = 0;
   int page = 1;
   int maxPage = 1;
   int mode = 0;
   int sortMode = 0;
   String keyword = "";

   static String desktopRoot = "C:\\Users\\Jihoon\\Desktop\\FileManager\\";
   String databaseRoot = desktopRoot + "db\\";
   String tempRoot = desktopRoot + "db\\temp\\";
   String chooserRoot = desktopRoot + "storage";
   String downloadRoot = desktopRoot + "download\\";
   String photoViewer = "mspaint.exe";
   String documentViewer = "notepad.exe";
   String musicPlayer = desktopRoot + "wmplayer.exe";
   static String thumbnailRoot = desktopRoot + "thumbnail\\thumb_"; // 맨 뒤에 파일명 머리글 추가

   String pattern = "";

   public static void main(String[] args) {
      FileManager frame = new FileManager();
      frame.setVisible(true);
   }

   /**
    * Create the frame.
    */
   public FileManager() {
      setTitle("File Manager Program");
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      makeGUI();

      setResizable(false);
      setBounds(100, 100, 1050, 600);
   }

   void makeGUI() {
      vFile = new Vector<FileInfo>();
      vImage = new Vector<FileInfo>();
      vDocum = new Vector<FileInfo>();
      vAudio = new Vector<FileInfo>();
      vEtc = new Vector<FileInfo>();
      vCurr = new Vector<FileInfo>();
      vSearch = new Vector<FileInfo>();
      vCurr = vFile;

      vFilePan = new Vector<MediaPanel>();
      vIfLab = new Vector<JLabel>();

      contentPane = new JPanel();
      contentPane.setBackground(new Color(175,206,225));
      contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
      setContentPane(contentPane);
      contentPane.setLayout(null);

      String lr1[] = {" ", "  "};
      String lr[] = {"images\\prev.png", "images\\next.png"};
      JButton lrBtn[] = new JButton[lr.length];
      for(int i = 0 ; i < lr.length ; i++) {
         lrBtn[i] = new JButton(lr1[i], new ImageIcon(lr[i]));
         contentPane.add(lrBtn[i]);
         lrBtn[i].setSize(80, 32);
         lrBtn[i].setLocation(150+i*145, 0);

         lrBtn[i].setBorderPainted(false);
         lrBtn[i].setFocusPainted(false);
         lrBtn[i].setContentAreaFilled(false);

         lrBtn[i].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               JButton b = (JButton)e.getSource();
               if(b.getText() == lr1[0]) page--;
               else if(b.getText() == lr1[1]) page++;
               refresh(0);
            }
         });
      }

      pageLa = new JTextField();
      pageLa.setText(Integer.toString(page));
      pageLa.setFont(new Font("Arial",pageLa.getFont().getStyle(),20));
      pageLa.setBounds(230, 3, 30, 28);
      pageLa.setBorder(null);
      pageLa.setHorizontalAlignment(SwingConstants.CENTER);
      contentPane.add(pageLa);
      pageLa.setColumns(10);
      pageLa.addMouseListener(new MouseAdapter() {
         public void mousePressed(MouseEvent e) {
            pageLa.setText("");
         }
      });
      pageLa.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            try {
               page = Integer.parseInt(pageLa.getText());
            } catch(NumberFormatException e1) {

            }

            System.out.println("page: " + page);
            if(page <= vFile.size()/(viewWid*viewHei)) {
               viewCnt = viewWid*viewHei;
            }
            else {
               viewCnt = vFile.size()%(viewWid*viewHei);
            }
            refresh(0);
         }
      });

      pageLa2 = new JLabel();
      pageLa2.setText("/ " + maxPage);

      pageLa2.setFont(new Font("Arial",pageLa.getFont().getStyle(),20));
      contentPane.add(pageLa2);
      pageLa2.setSize(30, 31);
      pageLa2.setLocation(265, 2);


      /* 파일정보창 */
      info_panel = new JPanel();
      info_panel.setBounds(40,405,552,119);
      contentPane.add(info_panel);
      makeInfoPanel(info_panel);

      /* 파일 컨트롤 버튼 */
      String controlStr[] = {"images\\upload1.png", "images\\download1.png", "images\\trash.png"};
      JButton controlBtn[] = new JButton[controlStr.length];
      for(int i = 0 ; i < controlBtn.length ; i++) {
         controlBtn[i] = new JButton(controlStr[i], new ImageIcon(controlStr[i]));
         controlBtn[i].setSize(80, 80);
         controlBtn[i].setLocation(630 + 122*i, 410);

         controlBtn[i].setBorderPainted(false);
         controlBtn[i].setFocusPainted(false);
         controlBtn[i].setContentAreaFilled(false);
         contentPane.add(controlBtn[i]);
      }


      String controltext[] = {"UPLOAD","DOWNLOAD","     DELETE"};
      JLabel controlLabel[] = new JLabel[controltext.length];
      for(int i=0; i<controlLabel.length; i++) {
         controlLabel[i] = new JLabel();
         controlLabel[i].setText(controltext[i]);
         controlLabel[i].setFont(new Font("Arial",controlLabel[i].getFont().getStyle(),20));
         controlLabel[i].setSize(150,80);
         controlLabel[i].setLocation(626 +109*i, 470);
         contentPane.add(controlLabel[i]);
      }

      /* 탑펜 */
      viewPanel = new JPanel();
      viewPanel.setBounds(174, 42, 840, 354);
      contentPane.add(viewPanel);
      makeIconPanel(viewPanel);

      /* 파일 포맷 버튼 */
      String formatStr[] = {"images\\label1.png", 
            "images\\label2.png",
            "images\\label3.png",
            "images\\label4.png",
      "images\\label5.png"};
      JButton formatBtn[] = new JButton[formatStr.length];
      for(int i = 0 ; i < formatBtn.length ; i++) {
         formatBtn[i] = new JButton(formatStr[i], new ImageIcon(formatStr[i]));
         formatBtn[i].setSize(150, 47);
         formatBtn[i].setLocation(40, 70 + 57*i);

         formatBtn[i].setBorderPainted(false);
         formatBtn[i].setFocusPainted(false);
         formatBtn[i].setContentAreaFilled(false);

         contentPane.add(formatBtn[i]);
         formatBtn[i].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               JButton b = (JButton)e.getSource();
               String format = b.getText();
               if(format == formatStr[0]) mode = 0;
               else if(format == formatStr[1]) mode = 1;
               else if(format == formatStr[2]) mode = 2;
               else if(format == formatStr[3]) mode = 3;
               else if(format == formatStr[4]) mode = 4;

               page = 1;
               //               refresh(0);
               sortFile(sortMode);
            }
         });
      }

      /***************************** 분류 시작 *****************************/

      Font sortFont = new Font("NanumGothic",Font.BOLD,15);
      JLabel sortTitle = new JLabel("분류");
      sortTitle.setOpaque(true);
      sortTitle.setBorder(null);
      sortTitle.setHorizontalAlignment(SwingConstants.CENTER);
      sortTitle.setBackground(Color.YELLOW);
      sortTitle.setForeground(Color.RED);
      sortTitle.setSize(80,20);
      sortTitle.setLocation(375,14);
      sortTitle.setFont(sortFont);
      contentPane.add(sortTitle);


      String sortText[] = {"저장순","이름순","형식순","날짜순"};
      JButton sortButton[] = new JButton[sortText.length];
      for(int i=0; i < sortButton.length; i++) {
         sortButton[i] = new JButton(sortText[i]);
         sortButton[i].setSize(80,20);
         sortButton[i].setLocation(455+i*80,14);
         sortButton[i].setBorderPainted(false);
         sortButton[i].setFocusPainted(false);
         //         sortButton[i].setBackground(Color.white);
         sortButton[i].setBackground(Color.CYAN);
         sortButton[i].setFont(sortFont);
         contentPane.add(sortButton[i]);
         sortButton[i].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               JButton c = (JButton)e.getSource();
               if(c.getText() == sortText[0]) sortMode = 0;
               else if(c.getText() == sortText[1]) sortMode = 1;
               else if(c.getText() == sortText[2]) sortMode = 2;
               else if(c.getText() == sortText[3]) sortMode = 3;
               sortFile(sortMode);
            }
         });
      }


      /***************************** 분류 종료 *****************************/

      /***************************** 검색창 시작 *****************************/
      textField = new JTextField();
      textField.setText("검색하기");
      //      textField.setBounds(813, 0, 200, 31);
      textField.setBounds(813, 0, 200, 25);
      textField.setBorder(null);
      contentPane.add(textField);
      textField.setColumns(10);

      textField.setFont(new Font("NanumGothic", Font.PLAIN, 15));
      //      textField.setHorizontalAlignment(SwingConstants.CENTER);

      textField.addMouseListener(new MouseAdapter() {
         public void mousePressed(MouseEvent e) {
            textField.setText("");
         }
      });
      textField.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            System.out.println(textField.getText());
            keyword = textField.getText();
            searchFile(keyword);
         }
      });
      /***************************** 검색창 종료 *****************************/


      controlBtn[0].addActionListener(new UploadActionListener());
      controlBtn[1].addActionListener(new DownloadActionListener());
      controlBtn[2].addActionListener(new DeleteActionListener());

      JLabel logo = new JLabel();
      logo.setBackground(Color.WHITE);
      logo.setFont(new Font("Ariel", Font.BOLD, 20));
      logo.setText("File Manager");
      contentPane.add(logo);
      logo.setSize(130, 30);
      logo.setLocation(12,2);

      makeBackground(contentPane);


      //////////////////////// 강지우
      initFile();

      this.addWindowListener(new WindowAdapter(){
         public void windowClosing(WindowEvent e) {

            FileInfo temp;

            System.out.println("size>>"+vFile.size());

            for(int i=0; i<vFile.size(); i++) {
               temp=vFile.elementAt(i);
               System.out.println(">>"+temp.getFileSize());
               uploadServer(temp);
            }

            File dirFile=new File(tempRoot);
            File []fileList=dirFile.listFiles();

            for(File tempFile : fileList) {
               tempFile.delete();
            }

            System.exit(0);
         }
      });
   }

   // 강지우
   void initFile() {
      File dirFile=new File(databaseRoot);
      File []fileList=dirFile.listFiles();

      FileInfo selfo;

      for(File tempFile : fileList) {
         if(tempFile.isFile()) {

            selfo = new FileInfo(tempFile);

            FileInputStream  fin  = null;
            FileOutputStream fout = null;
            int data = 0 ;

            try{
               fin = new FileInputStream(selfo.getAddr());
               fout = new FileOutputStream(tempRoot+ selfo.getFileName()); //확장명 jpg는 바꾸면 안됨
               while(true) {
                  data = fin.read();
                  if( data == -1) break;
                  fout.write(data); 
               }
               selfo.setAddr(tempRoot+ selfo.getFileName());
               vFile.addElement(selfo);
               System.out.println();
               System.out.println(selfo.getFileName() + " 저장 완료");

            }
            catch(IOException e1) {
               System.out.println("파일오픈실패");
               e1.printStackTrace();
            }
            finally {
               try   {
                  fin.close();
                  fout.close();
               }
               catch(Exception e1) {

               }
            }
            System.out.println("작업종료");

            tempFile.delete();
            fileIO();
            searchFile(keyword);
            //         refresh(0);
            sortFile(sortMode);
         }
      }
   }

   void makeBackground(JPanel p) {
      JPanel top = new JPanel();
      top.setBackground(new Color(65,140,255));
      p.add(top);
      top.setBounds(-5, -5, 1200, 39);
   }

   void sortFile(int s) {
      refresh(0);

      if(s == 0) return;
      if(vCurr.size() == 0) return;

      Vector<FileInfo> vSort = new Vector<FileInfo>();
      vSort.addElement(vCurr.elementAt(0));

      if(vCurr.size() == 1) return;

      for(int i = 1 ; i < vCurr.size() ; i++) {
         int cnt = 0;
         while(true) {
            String sStr = null, cStr = null;
            switch (s) {
            case 1:
               sStr = vSort.elementAt(cnt).getFileName();
               cStr = vCurr.elementAt(i).getFileName();
               break;
            case 2:
               sStr = vSort.elementAt(cnt).getExt();
               cStr = vCurr.elementAt(i).getExt();
               break;
            case 3:
               sStr = vSort.elementAt(cnt).getDate();
               cStr = vCurr.elementAt(i).getDate();
               break;
            default:
               System.out.println("Error!!");
            }
            sStr = sStr.toLowerCase();
            cStr = cStr.toLowerCase();

            if(sStr.compareTo(cStr) > 0) {
               vSort.add(cnt, vCurr.elementAt(i));
               break;
            }
            cnt++;
            if(vSort.size() <= cnt) {
               vSort.addElement(vCurr.elementAt(i));
               break;
            }
         }
      }

      //      for(int i = 0 ; i < vSort.size() ; i++)
      //         System.out.print(vSort.elementAt(i).getFileName()+" -> ");
      //      System.out.println();

      vCurr = vSort;
      refresh(1);
   }

   void searchFile(String sch) {
      // 자바로 검색기능 구현하기: http://blog.naver.com/gdpswpf007/220915010331
      vSearch.removeAllElements();
      for(int i = 0 ; i < vFile.size() ; i++) {
         if(vFile.elementAt(i).getFileName().toLowerCase().indexOf(sch.toLowerCase())!=-1){
            //            System.out.println(vFile.elementAt(i).getFileName());
            vSearch.addElement(vFile.elementAt(i));
         }
      }
      mode = 5;
      //      refresh(0);
      sortFile(sortMode);
   }

   void makeIconPanel(JPanel p) {
      p.setLayout(new GridLayout(viewHei, viewWid));

      for(int i = 0 ; i < viewWid*viewHei ; i++) {
         MediaPanel mPan = new MediaPanel();
         vFilePan.addElement(mPan);
         mPan.setVisible(false);
         p.add(mPan);
      }
   }

   void makeListPanel(JPanel p) {
      p.setLayout(new BorderLayout());
      list = new JList(vCurr);
      list.addMouseListener(new MouseAdapter() {
         public void mouseClicked(MouseEvent e) {
            FileInfo fi = (FileInfo)list.getSelectedValue();
            System.out.println(fi);
            if(list.getSelectedIndex() > -1){
               targetFile = fi;
               viewInfoPanel();
               list.updateUI();
            }
         }
      });
      p.add(new JScrollPane(list));
   }

   void makeInfoPanel(JPanel p) {
      p.setLayout(new BorderLayout());


      JPanel innerPan[] = new JPanel[2];
      String str[] = {"  파일경로   ", "  파  일  명   ", "  수정날짜", "  크         기", "  숨김여부", "  파일타입"};

      for(int i = 0 ; i < 2 ; i++) {
         innerPan[i] = new JPanel();
         innerPan[i].setLayout(new GridLayout(str.length, 1));
         innerPan[i].setBackground(new Color(235,235,235));
      }
      p.add(innerPan[0], BorderLayout.WEST);
      p.add(innerPan[1], BorderLayout.CENTER);

      for(int i = 0 ; i < str.length ; i++) {
         JLabel la = new JLabel();
         //         la.setFont(new Font("NanumGothic",Font.BOLD,15));
         innerPan[0].add(new JLabel(str[i]));
         innerPan[1].add(la);
         vIfLab.addElement(la);
      }
   }

   void viewInfoPanel() {
      vIfLab.elementAt(0).setText(targetFile == null ? "" : targetFile.getAddr());
      vIfLab.elementAt(1).setText(targetFile == null ? "" : targetFile.getFileName());
      vIfLab.elementAt(2).setText(targetFile == null ? "" : targetFile.getDate());
      vIfLab.elementAt(3).setText(targetFile == null ? "" : targetFile.getSizeString());
      vIfLab.elementAt(4).setText(targetFile == null ? "" : (targetFile.getHide()? "숨김": "공개"));
      String type = "";
      switch(targetFile.getType()) {
      case 'i': type = "IMAGE"; break;
      case 'd': type = "DOCUMENT"; break;
      case 'a': type = "AUDIO"; break;
      default: type = "UNKNOWN"; break;
      }
      vIfLab.elementAt(5).setText(targetFile == null ? "" : type);
      info_panel.updateUI();
   }

   class FileInfo {
      String address;
      String thumbIMG;
      String fileName;
      String date;
      int fileSize;
      int size;
      String unit;
      char type;
      String ext;
      boolean hide = false;

      FileInfo(FileInfo fi) {
         address = fi.getAddr();
         thumbIMG = fi.getThumbIMG();
         fileName = fi.getFileName();
         date = fi.getDate();
         fileSize = fi.getFileSize();
         size = fi.getSize();
         unit = fi.getUnit();
         type = fi.getType();
         ext = fi.getExt();
         hide = fi.getHide();
      }

      FileInfo(File f) {
         address = f.getPath();
         fileName = f.getName();
         date = new SimpleDateFormat("yyyy-MM-dd / HH:mm:ss").format(f.lastModified());
         fileSize = (int)f.length();
         sizeComp();
         type = searchFileType();
         createThumbnail();
      }

      public char searchFileType() {
         String str = "";
         str += fileName.charAt(fileName.length()-3);
         str += fileName.charAt(fileName.length()-2);
         str += fileName.charAt(fileName.length()-1);
         ext = str.toLowerCase();

         switch(ext) {
         case "jpg":
         case "png":
         case "bmp":
         case "gif": return 'i';
         case "txt":
         case "hwp": return 'd';
         case "mp3":
         case "wav":
         case "mp4": return 'a';
         default: return 'x';
         }
      }

      // 파일 사이즈 짧게 조정
      public void sizeComp() {
         int temp = fileSize;
         int s = 0;
         int u = 0;

         while(temp > 0) {
            if(temp/1024 > 0) u++;
            else s = temp;
            temp /= 1024;
         }
         size = s;
         switch(u) {
         case 0: unit = "Byte"; break;
         case 1: unit = "KB"; break;
         case 2: unit = "MB"; break;
         default: unit = "GB"; break;
         }
      }

      public void createThumbnail() {
         if(type == 'i')
            thumbIMG = createImageThumbnail(address,fileName);
      }

      public void setAddr(String addr) {
         address = addr;
      }

      public void setThumbIMG(String thumb) {
         thumbIMG = thumb;
      }

      public void setDate(String dt) {
         date = dt;
      }

      public void setFileName(String fn) {
         fileName = fn;
      }

      public void setFileSize(int s) {
         fileSize = s;
      }

      public void setHide(boolean h) {
         hide = h;
      }

      public void setType(char t) {
         type = t;
      }

      public String getAddr() {
         return address;
      }

      public String getThumbIMG() {
         return thumbIMG;
      }

      public String getDate() {
         return date;
      }

      public String getFileName() {
         return fileName;
      }

      public int getFileSize() {
         return fileSize;
      }

      public int getSize() {
         return size;
      }

      public String getUnit() {
         return unit;
      }

      public String getSizeString() {
         return Integer.toString(size) + unit;
      }

      public boolean getHide() {
         return hide;
      }

      public char getType() {
         return type;
      }

      public String getExt() {
         return ext;
      }

      // 파일의 정보 확인
      public void showFileInfo() {
         System.out.println("절대경로: " + address);
         System.out.println("파 일 명: " + fileName);
         System.out.println("수정날짜: " + date);
         System.out.println("크     기: " + fileSize);
         System.out.println("숨김여부: " + hide);
         System.out.println("파일타입: " + type);
      }

      public String toString() {
         return fileName;
      }
   }


   class UploadActionListener implements ActionListener {
      JFileChooser chooser;

      UploadActionListener() {
         chooser = new JFileChooser(chooserRoot);
         initFileChooser(chooser);
      }

      public void actionPerformed(ActionEvent e) {
         int ret = chooser.showOpenDialog(null);
         if (ret != JFileChooser.APPROVE_OPTION) {
            //            JOptionPane.showMessageDialog(null, "파일을 선택하지 않았습니다", "경고", JOptionPane.WARNING_MESSAGE);
            return;
         }

         File selectedFile = chooser.getSelectedFile();
         FileInfo selInfo = new FileInfo(selectedFile);
         vFile.addElement(new FileInfo(selectedFile));

         switch(selInfo.getType()) {
         case 'i': vImage.addElement(selInfo); break;
         case 'd': vDocum.addElement(selInfo); break;
         case 'a': vAudio.addElement(selInfo); break;
         case 'x': vEtc.addElement(selInfo); break;
         }

         fileIO();
         searchFile(keyword);
         //         refresh(0);
         sortFile(sortMode);
      }
   }

   public void fileIO() {
      page = vCurr.size()/(viewWid*viewHei) + 1;
      viewCnt = (vCurr.size()-1)%(viewWid*viewHei);
      viewCnt++;
   }

   public void refresh(int num) {
      System.out.println(mode);
      if (num == 0) {
         switch(mode) {
         case 0: vCurr = vFile; break;
         case 1: vCurr = vImage; break;
         case 2: vCurr = vDocum; break;
         case 3: vCurr = vAudio; break;
         case 4: vCurr = vEtc; break;
         case 5: vCurr = vSearch; break;
         }
      }

      if(page < 1) page = 1;
      else if(page > (vCurr.size()-1)/(viewWid*viewHei) + 1) page = (vCurr.size()-1)/(viewWid*viewHei) + 1;
      if(page <= vCurr.size()/(viewWid*viewHei)) 
         viewCnt = viewWid*viewHei;
      else 
         viewCnt = vCurr.size()%(viewWid*viewHei);
      maxPage = (vCurr.size()-1)/(viewWid*viewHei) + 1;
      pageLa.setText(page+"");
      pageLa2.setText("/ " + maxPage);

      for(int i = 0 ; i < viewWid*viewHei ; i++) {
         vFilePan.elementAt(i).setVisible(false);
         vFilePan.elementAt(i).mBtn.setIcon(null);
         vFilePan.elementAt(i).mBtn.setText(null);
      }

      for(int i = 0 ; i < viewCnt ; i++) {
         MediaPanel mPan = vFilePan.elementAt(i);
         mPan.setMediaPanel(i);
         mPan.setVisible(true);
      }
      viewPanel.updateUI();
   }

   class DownloadActionListener implements ActionListener {
      public void actionPerformed(ActionEvent e) {
         if(targetFile == null) {
            System.out.println("지정된 파일이 없습니다.");
            return;
         }

         FileInputStream  fin  = null;
         FileOutputStream fout = null;
         int data = 0 ;

         try{
            fin = new FileInputStream(targetFile.getAddr());
            fout = new FileOutputStream(downloadRoot + targetFile.getFileName()); //확장명 jpg는 바꾸면 안됨
            while(true) {
               data = fin.read();
               if( data == -1) break;
               fout.write(data); 
            }
            System.out.println();
            System.out.println(targetFile.getFileName() + " 저장 완료");
         }
         catch(IOException e1) {
            System.out.println("파일오픈실패");
            e1.printStackTrace();
         }
         finally {
            try   {
               fin.close();
               fout.close();
            }
            catch(Exception e1) {

            }
         }
         System.out.println("작업종료");
      }
   }

   class DeleteActionListener implements ActionListener {
      JFileChooser chooser;

      DeleteActionListener() {
         chooser = new JFileChooser(chooserRoot);
         initFileChooser(chooser);
      }

      public void actionPerformed(ActionEvent e) {
         if(targetFile == null) {
            System.out.println("지정파일 없음");
            return;
         }

         System.out.println(targetFile.toString() + " 삭제되었습니다.");
         switch(targetFile.getType()) {
         case 'i': vImage.remove(targetFile); break;
         case 'd': vDocum.remove(targetFile); break;
         case 'a': vAudio.remove(targetFile); break;
         default: vEtc.remove(targetFile); break;
         }
         vFile.remove(targetFile);
         vSearch.remove(targetFile);

         targetFile = null;

         fileIO();
         refresh(0);
      }
   }

   //강지우
   void uploadServer(FileInfo in) {      
      FileInputStream  fin  = null;
      FileOutputStream fout = null;
      int data = 0 ;

      try{
         fin = new FileInputStream(in.getAddr());
         fout = new FileOutputStream(databaseRoot + in.getFileName()); //확장명 jpg는 바꾸면 안됨
         while(true) {
            data = fin.read();
            if( data == -1) break;
            fout.write(data); 
         }
         System.out.println();
         System.out.println(in.getFileName() + " 저장 완료");

      }
      catch(IOException e1) {
         System.out.println("파일오픈실패");
         e1.printStackTrace();
      }
      finally {
         try   {
            fin.close();
            fout.close();
         }
         catch(Exception e1) {

         }
      }
      System.out.println("작업종료");



   }

   // 파일 다이얼로그 선택자
   void initFileChooser(JFileChooser fc) {
      fc.addChoosableFileFilter(new FileNameExtensionFilter("images (JPG, PNG, BMP, GIF)", "jpg", "png", "bmp", "gif"));
      fc.addChoosableFileFilter(new FileNameExtensionFilter("Document (HWP, TXT)", "hwp", "txt"));
      fc.addChoosableFileFilter(new FileNameExtensionFilter("Audio (MP3, WAV, MP4)", "mp3", "wav", "mp4"));
   }

   class MediaPanel extends JPanel {
      FileInfo info;
      MediaButton mBtn;
      ImageIcon imageIcon;
      Image image, newimg;
      JLabel la;
      MediaPanel() {
         setLayout(new BorderLayout());

         mBtn = new MediaButton();
         la = new JLabel();

         add(mBtn, BorderLayout.CENTER);
         add(la, BorderLayout.SOUTH);
      }

      MediaPanel(int num) {
         setMediaPanel(num);
         info = vCurr.elementAt(num);
         la.setText(info.getFileName());
      }

      public void setMediaPanel(int num) {
         switch(vCurr.elementAt(num + (page-1)*viewWid*viewHei).type) {
         case 'i':
            imageIcon = new ImageIcon(vCurr.elementAt(num + (page-1)*viewWid*viewHei).getThumbIMG());
            mBtn.setImage(imageIcon, vCurr.elementAt(num + (page-1)*viewWid*viewHei));
            break;
         case 'd': 
            mBtn.setDocum(vCurr.elementAt(num + (page-1)*viewWid*viewHei));
            break;
         case 'a':
            imageIcon = new ImageIcon("images\\audio.png");
            mBtn.setImage(imageIcon, vCurr.elementAt(num + (page-1)*viewWid*viewHei));
            break;
         default:
            mBtn.setText("?");
            mBtn.setUnk(vCurr.elementAt(num + (page-1)*viewWid*viewHei));
            break;
         }
         info = vCurr.elementAt(num + (page-1)*viewWid*viewHei);
         la.setText(info.getFileName());

      }

   }

   class MediaButton extends JButton {
      FileInfo info;
      MediaButton() {
         setBorderPainted(false);
         setFocusPainted(false);
         setContentAreaFilled(false);

         addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               MediaButton b = (MediaButton)e.getSource();

               if(b.getFileInfo() == targetFile) {
                  Runtime r1 = Runtime.getRuntime();
                  switch(info.getType()) {
                  case 'i':
                     try {
                        //프로그램명 매개변수  넣어주면 실행
                        //파일 경로명 넣어주면 경로명에 있는 파일이 노트패드로 실행
                        r1.exec(photoViewer + " " + targetFile.getAddr()); 
                     } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                     }
                     break;
                  case 'd':
                     try {
                        //프로그램명 매개변수  넣어주면 실행
                        //파일 경로명 넣어주면 경로명에 있는 파일이 노트패드로 실행
                        r1.exec(documentViewer + " " + targetFile.getAddr()); 
                     } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                     }
                     break;
                  case 'a':
                     try {
                        //프로그램명 매개변수  넣어주면 실행
                        //파일 경로명 넣어주면 경로명에 있는 파일이 노트패드로 실행
                        r1.exec(musicPlayer + " " + targetFile.getAddr()); 
                     } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                     }
                     break;
                  default:
                     System.out.println("실행할 파일이 설정되지 않았습니다.");
                  }
               }

               targetFile = b.getFileInfo();
               targetFile.showFileInfo();
               viewInfoPanel();
               info_panel.updateUI();
            }
         });
      }

      MediaButton(FileInfo fi) {
         info = fi;
      }

      public void setImage(ImageIcon ic, FileInfo fi) {
         setIcon(ic);
         info = fi;
      }

      public void setAudio(ImageIcon ic, FileInfo fi) {
         setIcon(ic);
         info = fi;
      }

      public void setDocum(FileInfo fi) {
         String str[] = new String[3];
         for(int i = 0 ; i < 3 ; i++)
            str[i] = new String();
         try {
            FileReader reader = new FileReader(fi.getAddr());
            int ch = 0;
            int cnt = 0;

            while((ch = reader.read()) != -1) {
               if(ch == '\n') str[cnt] += " ";
               else str[cnt] += (char)ch;
               if(str[cnt].length() > 9) cnt++;
               if(cnt >= 3) break;
            }
            reader.close();
         } catch(FileNotFoundException e) {

         } catch(IOException e) {

         }
         setText("<html>" + str[0] + "<br>" + str[1] + "<br>" + str[2] + "</html>");
         info = fi;
      }

      public void setUnk(FileInfo fi) {
         info = fi;
      }

      public FileInfo getFileInfo() {
         return info;
      }

   }

   void pageInfo(Vector<FileInfo> v) {
      System.out.println("---------------------------------------\n");
      System.out.println("vector size(): " + v.size());
      System.out.println("vFileBtn size(): " + vFilePan.size());
      System.out.println("viewCnt: " + viewCnt);
      System.out.println("page: " + page);
      System.out.println("---------------------------------------\n");
   }


   /**
    * 이미지 섬네일 생성(기본 사이즈:150×150)
    * @param srcPath 원본 파일 경로
    * @param srcFileNm 원본 파일 명
    * @return
    */
   public static String createImageThumbnail(String srcPath, String srcFileNm) {
      return createImageThumbnail(srcPath, srcFileNm, 155, 90);
   }

   public static String createImageThumbnail(String srcPath, String srcFileNm, int width, int height) {
      String destPath = null;
      if( srcPath==null || srcFileNm==null ) {
         return "";
      }

      try {
         BufferedImage buffImage = ImageIO.read(new File(srcPath));

         ResampleOp resampleOp = new ResampleOp(width, height);
         resampleOp.setUnsharpenMask( AdvancedResizeOp.UnsharpenMask.Soft );
         BufferedImage rescaledImage = resampleOp.filter(buffImage, null);

         destPath = thumbnailRoot + srcFileNm;
         File destFile = new File(destPath);

         ImageIO.write(rescaledImage, "png", destFile);
      } catch (IOException e) {
         e.printStackTrace();
      }

      return destPath;
   }

}
