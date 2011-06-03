/*
 * Main.java
 * Refer to "Fast and Enhanced Algorithm for Examplar Based Image Inpainting"
 *
 * Copyright (C) 2010-2011  Sapan Diwakar and Pulkit Goyal
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Please contact Sapan Diwakar or Pulkit Goyal
 * diwakar.sapan@gmail.com or pulkit110@gmail.com or visit
 * <http://sapandiwakar.wordpress.com> or <http://pulkitgoyal.wordpress.com>
 * if you need any additional information or have any questions. 
 */

package imageselection;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.UIManager;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import inpaint.*;

/**
 * Class to make UI and communicate the ImageInpainting module
 * @author Pulkit & Sapan
 */
public class Main extends JFrame implements Runnable{
    
    public Entry entry;                             // Defines the object of Entry class
    protected Image entryImage;                     // variable of type Image
    JScrollPane pictureScrollPane;                  // Scrollpane for scrolling Images(whenever required)
    File outputFile;                                // file used while saving the image
    String fileExtension;                           // string to store the extension of a image
    ImageInpaint Inpainter;                         // Defines the object of ImageInpaint class
    Thread inpaintThread = null;                    // thread for the inpaint process
    private Boolean fastInpaint = false;            // flag to determine fastInpaint
    //Button playbtn;

    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JFrame jFrame5;
    private javax.swing.JFrame jFrame6;
    private javax.swing.JFrame jFrame7;
    private javax.swing.JToolBar jToolBar1;
    
    @SuppressWarnings("deprecation")
    Main()
    {
        Inpainter = new ImageInpaint(this);         // Creates an instance of the ImageInpaint class

        /**
         * sets look and feel for the UI
         */
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
        } catch (Exception e) {
            e.printStackTrace();
        }

        /**
         * Set the icon for the UI
         */
        try {
            Image i = ImageIO.read(getClass().getResource("/imageselection/Images/logo.png"));
            setIconImage(i);
        } catch (IOException ex) {
            Logger.getLogger(Entry.class.getName()).log(Level.SEVERE, null, ex);
        }

        /**
         * Action to be performed on clicking the close button
         */
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        initComponents();

        /**
         * Reads the image to be inpainted.
         */
        try {
            entryImage = ImageIO.read(getClass().getResource("/imageselection/Images/defaultImage.png"));
        } catch (IOException ex) {
            Logger.getLogger(Entry.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        setTitle("Image Inpainting");                         // Set the title of the Window
        getContentPane().setLayout(null);
        setSize(500,500);                                     //Set Window Width and Height
       
        entry = new Entry(entryImage);                        // creates an instance of the entry class
        
        entry.setPreferredSize(new Dimension(entryImage.getWidth(this),entryImage.getHeight(this)));
        
        getContentPane().add(entry);
        entry.initImage();  
        pictureScrollPane = new JScrollPane();                // creates an instance of the JScrollPane class
        getContentPane().add(pictureScrollPane);
        int w = Math.min(entryImage.getWidth(this)+3,getContentPane().getWidth());
        int h = Math.min(entryImage.getHeight(this)+3,getContentPane().getHeight()-50);
        if (h == entryImage.getHeight(this)+3) {
            pictureScrollPane.setBounds((getContentPane().getWidth()-w)/2,(getContentPane().getHeight()-h)/2, w, h);
        } else {
            pictureScrollPane.setBounds((getContentPane().getWidth()-w)/2,(getContentPane().getHeight()-h)/2, w, h+25);
        }
        
        pictureScrollPane.setAlignmentY(CENTER_ALIGNMENT);
        pictureScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        pictureScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pictureScrollPane.setViewportView(entry);
        
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int w = Math.min(entryImage.getWidth(entry)+3,getContentPane().getWidth());
                int h = Math.min(entryImage.getHeight(entry)+3,getContentPane().getHeight()-50);
                if (h == entryImage.getHeight(entry)+3) {
                    pictureScrollPane.setBounds((getContentPane().getWidth()-w)/2,(getContentPane().getHeight()-h)/2, w, h);
                } else {
                    pictureScrollPane.setBounds((getContentPane().getWidth()-w)/2,(getContentPane().getHeight()-h)/2, w, h+25);
                }
         //       pictureScrollPane.setBounds((getContentPane().getWidth()-w)/2,(getContentPane().getHeight()-h)/2, w, h);
                pictureScrollPane.setViewportView(entry);
            }
        });
    }

    /**
     * main function execution starts from here
     * @param args command line argument
     */
    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        (new Main()).show();
    }
    
     private void initComponents() {
        JFrame jFrame1 = new javax.swing.JFrame();
        JFrame jFrame2 = new javax.swing.JFrame();
        JFrame jFrame3 = new javax.swing.JFrame();
        JFrame jFrame4 = new javax.swing.JFrame();



        JMenuBar jMenuBar1 = new javax.swing.JMenuBar();
        
        JMenu jMenu1 = new javax.swing.JMenu();
        JMenu jMenu2 = new javax.swing.JMenu();
        JMenu jMenu3 = new javax.swing.JMenu();
        JMenu jMenu4 = new javax.swing.JMenu();
        
        JMenuItem jMenuItem1 = new javax.swing.JMenuItem();
        JMenuItem jMenuItem2 = new javax.swing.JMenuItem();
        JMenuItem jMenuItem3 = new javax.swing.JMenuItem();        
        JMenuItem jMenuItem4 = new javax.swing.JMenuItem();
        JMenuItem jMenuItem5 = new javax.swing.JMenuItem();        
        JMenuItem jMenuItem6 = new javax.swing.JMenuItem();
        JMenuItem jMenuItem7 = new javax.swing.JMenuItem();
        JMenuItem jMenuItem8 = new javax.swing.JMenuItem();
        JMenuItem jMenuItem9 = new javax.swing.JMenuItem();

        javax.swing.GroupLayout jFrame1Layout = new javax.swing.GroupLayout(jFrame1.getContentPane());
        jFrame1.getContentPane().setLayout(jFrame1Layout);
        jFrame1Layout.setHorizontalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame1Layout.setVerticalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jFrame2Layout = new javax.swing.GroupLayout(jFrame2.getContentPane());
        jFrame2.getContentPane().setLayout(jFrame2Layout);
        jFrame2Layout.setHorizontalGroup(
            jFrame2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame2Layout.setVerticalGroup(
            jFrame2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jFrame3Layout = new javax.swing.GroupLayout(jFrame3.getContentPane());
        jFrame3.getContentPane().setLayout(jFrame3Layout);
        jFrame3Layout.setHorizontalGroup(
            jFrame3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame3Layout.setVerticalGroup(
            jFrame3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jFrame4Layout = new javax.swing.GroupLayout(jFrame3.getContentPane());
        jFrame4.getContentPane().setLayout(jFrame4Layout);
        jFrame4Layout.setHorizontalGroup(
            jFrame4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame4Layout.setVerticalGroup(
            jFrame4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jMenu1.setText("File");

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imageselection/Images/open.png")));
        jMenuItem1.setText("Open Image");
        jMenu1.add(jMenuItem1);
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imageselection/Images/save.png")));
        jMenuItem2.setText("Save");
        jMenu1.add(jMenuItem2);
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });

        jMenuItem7.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imageselection/Images/saveAs.png")));
        jMenuItem7.setText("Save As");
        jMenu1.add(jMenuItem7);
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        
        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
        jMenuItem3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imageselection/Images/exit.png")));
        jMenuItem3.setText("Exit");
        jMenu1.add(jMenuItem3);
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");

        jMenuItem4.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imageselection/Images/undo.png")));
        jMenuItem4.setText("Undo");
        jMenu2.add(jMenuItem4);
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });

        jMenuItem5.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imageselection/Images/redo.png")));
        jMenuItem5.setText("Redo");
        jMenu2.add(jMenuItem5);
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });

        jMenuBar1.add(jMenu2);


        jMenu3.setText("Inpaint");

        jMenuItem8.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        //jMenuItem8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imageselection/Images/open.png")));
        jMenuItem8.setText("Run");
        jMenu3.add(jMenuItem8);
        jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem8ActionPerformed(evt);
            }
        });

        jMenuItem9.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        //jMenuItem9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imageselection/Images/open.png")));
        jMenuItem9.setText("Fast Inpaint");
        jMenu3.add(jMenuItem9);
        jMenuItem9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem9ActionPerformed(evt);
            }
        });

        jMenu4.setText("Help");

        jMenuItem6.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        jMenuItem6.setText("Help");
        jMenu4.add(jMenuItem6);
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }            
        });
        
        jMenuBar1.add(jMenu3);

        jMenuBar1.add(jMenu4);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 279, Short.MAX_VALUE)
        );

        jFrame5 = new javax.swing.JFrame();
        jFrame6 = new javax.swing.JFrame();
        jFrame7 = new javax.swing.JFrame();
        jToolBar1 = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();

        javax.swing.GroupLayout jFrame5Layout = new javax.swing.GroupLayout(jFrame5.getContentPane());
        jFrame5.getContentPane().setLayout(jFrame5Layout);
        jFrame5Layout.setHorizontalGroup(
            jFrame5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame5Layout.setVerticalGroup(
            jFrame5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jFrame6Layout = new javax.swing.GroupLayout(jFrame6.getContentPane());
        jFrame6.getContentPane().setLayout(jFrame6Layout);
        jFrame6Layout.setHorizontalGroup(
            jFrame6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame6Layout.setVerticalGroup(
            jFrame6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jFrame7Layout = new javax.swing.GroupLayout(jFrame7.getContentPane());
        jFrame7.getContentPane().setLayout(jFrame7Layout);
        jFrame7Layout.setHorizontalGroup(
            jFrame7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame7Layout.setVerticalGroup(
            jFrame7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jToolBar1.setRollover(true);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imageselection/Images/open.png"))); // NOI18N
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton1);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imageselection/Images/save.png"))); // NOI18N
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton2);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imageselection/Images/undo.png"))); // NOI18N
        jButton3.setFocusable(false);
        jButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton3);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imageselection/Images/redo.png"))); // NOI18N
        jButton4.setFocusable(false);
        jButton4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton4);
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });

        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imageselection/Images/play-button.png"))); // NOI18N
        jButton6.setFocusable(false);
        jButton6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton6.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton6);
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem9ActionPerformed(evt);
            }
        });

        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imageselection/Images/pause-button.png"))); // NOI18N
        jButton7.setFocusable(false);
        jButton7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton7.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton7);
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PauseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout1 = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout1);
        layout1.setHorizontalGroup(
            layout1.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout1.setVerticalGroup(
            layout1.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout1.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(275, Short.MAX_VALUE))
        );

        pack();
    }

     /**
      * action performed for opening file
      * @param evt
      */
     private void jMenuItem1ActionPerformed(ActionEvent evt) {

         if (!entry.isDisabled) {

             BufferedImage selectedImage;

             JFileChooser _fileChooser = new JFileChooser();
             int retval = _fileChooser.showOpenDialog(Main.this);

             /**
              * extensions of images user is allowed to choose
              */
             final String[] okFileExtensions = new String[] {"jpg", "png", "gif", "bmp", "jpeg"};
             File file;

             if (retval == JFileChooser.APPROVE_OPTION) {
                try {
                    file = _fileChooser.getSelectedFile();
                    Boolean flag = false;
                    for (String extension : okFileExtensions) {
                        if (file.getName().toLowerCase().endsWith(extension)) {
                            outputFile = file;
                            fileExtension = extension;
                            flag = true;
                        }
                    }
                    if (!flag) {
                        JOptionPane.showMessageDialog(this, "Please choose a jpg, jpeg, png, bmp or gif file only.","Error",JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    entry.SavedImages.clear();
                    entry.RedoImages.clear();
                    selectedImage = ImageIO.read(file);
                    Image tmg = createImage(((Image)selectedImage).getWidth(this),((Image)selectedImage).getHeight(this));
                    Graphics tg = tmg.getGraphics();
                    tg.drawImage((Image)selectedImage,0,0,null);
                    entry.SavedImages.push(selectedImage);
                    entryImage = tmg;
                    entry.showImage(entryImage);
                    entry.setPreferredSize(new Dimension(entryImage.getWidth(this),entryImage.getHeight(this)));
                    int w = Math.min(entryImage.getWidth(this)+3,getContentPane().getWidth());
                    int h = Math.min(entryImage.getHeight(this)+3,getContentPane().getHeight());
                    pictureScrollPane.setBounds((getContentPane().getWidth()-w)/2,(getContentPane().getHeight()-h)/2, w, h);
                    pictureScrollPane.setViewportView(entry);
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
          }
         }
     }

     /**
      * action performed for undo event
      * @param evt
      */
    @SuppressWarnings("unchecked")
      private void jMenuItem4ActionPerformed(ActionEvent evt)
      {
        Boolean flag = false;

         if ((!entry.isDisabled) && (entry.SavedImages.size()>1)) {
            if (entry.getPressed()) {
                entry.entryReset();
                flag = true;
            }
            entry.RedoImages.push(entry.SavedImages.pop());
            Image tmg = createImage(((Image)entry.SavedImages.peek()).getWidth(this),((Image)entry.SavedImages.peek()).getHeight(this));
            Graphics tg = tmg.getGraphics();
            tg.drawImage((Image)entry.SavedImages.peek(),0,0,null);
            entry.showImage(tmg);
            
            entry.setPreferredSize(new Dimension(entryImage.getWidth(this),entryImage.getHeight(this)));
            int w = Math.min(entryImage.getWidth(this)+3,getContentPane().getWidth());
            int h = Math.min(entryImage.getHeight(this)+3,getContentPane().getHeight());
            pictureScrollPane.setBounds((getContentPane().getWidth()-w)/2,(getContentPane().getHeight()-h)/2, w, h);
            pictureScrollPane.setViewportView(entry);

            if (flag) {
                jMenuItem5ActionPerformed(evt);
            }
         }
      }

    /**
      * action performed for redo event
      * @param evt
      */
    @SuppressWarnings("unchecked")
     private void jMenuItem5ActionPerformed(ActionEvent evt)
     {
         if ((!entry.isDisabled) && (entry.RedoImages.size()>0)) {
            
            Image tmg = createImage(((Image)entry.RedoImages.peek()).getWidth(this),((Image)entry.RedoImages.peek()).getHeight(this));
            Graphics tg = tmg.getGraphics();
            tg.drawImage((Image)entry.RedoImages.peek(),0,0,null);
            entry.showImage(tmg);
            entry.SavedImages.push(entry.RedoImages.pop());

            entry.setPreferredSize(new Dimension(entryImage.getWidth(this),entryImage.getHeight(this)));
            int w = Math.min(entryImage.getWidth(this)+3,getContentPane().getWidth());
            int h = Math.min(entryImage.getHeight(this)+3,getContentPane().getHeight());
            pictureScrollPane.setBounds((getContentPane().getWidth()-w)/2,(getContentPane().getHeight()-h)/2, w, h);
            pictureScrollPane.setViewportView(entry);
         }
      }
    private void jMenuItem6ActionPerformed(ActionEvent evt)
     {
        (new Help()).show();
     }

    /**
      * action performed for saving image
      * @param evt
      */
     private void jMenuItem2ActionPerformed(ActionEvent evt)
     {         
         if (outputFile == null) {
             System.err.println("Error!! No file to save");
             return;
         }
         
         try {
             BufferedImage bi = (BufferedImage)entry.getImage();
             ImageIO.write(bi, fileExtension, outputFile);
         } catch (IOException e) {
             System.err.println("Error!! File not saved");
         }
     }

     /**
      * action performed when user wants to save image with different name
      * @param evt
      */
     @SuppressWarnings("unchecked")
      private void jMenuItem7ActionPerformed(ActionEvent evt)
      {         
         JFileChooser _fileChooser = new JFileChooser();
         int retval = _fileChooser.showSaveDialog(Main.this);

         final String[] okFileExtensions = new String[] {"jpg", "png", "gif", "bmp", "jpeg"};
         File file;

         if (retval == JFileChooser.APPROVE_OPTION) {             
            file = _fileChooser.getSelectedFile();
            Boolean flag = false;
            for (String extension : okFileExtensions) {
                if (file.getName().toLowerCase().endsWith(extension)) {
                    if (outputFile == null) {                            
                        System.err.println("Error!! No file to save");
                        return;
                    }
                    try {
                        outputFile = file;
                        fileExtension = extension;
                        BufferedImage bi = (BufferedImage)entry.getImage();
                        ImageIO.write(bi, fileExtension, outputFile);
                        System.out.println("Saved");
                    } catch (IOException e) {
                        System.err.println("Error!! File not saved");
                    }


                    flag = true;
                }
            }
            if (!flag) {
                JOptionPane.showMessageDialog(this, "Please choose a jpg, jpeg, png, bmp or gif file only.","Error",JOptionPane.ERROR_MESSAGE);
                return;
            }
        } 
       
      }

     /**
      * action performed for inpaint
      * @param evt
      */
     private void jMenuItem8ActionPerformed(ActionEvent evt) {
         Inpainter.halt = false;
         Inpainter.completed = false;
          if ( inpaintThread==null ) {
              entry.setDisabled();
              inpaintThread = new Thread(this);
              inpaintThread.start();
          }
     }

     /**
      * action performed for fast inpaint
      */
     private void jMenuItem9ActionPerformed(ActionEvent evt) {
         fastInpaint = true;
         Inpainter.halt = false;
         Inpainter.completed = false;
          if ( inpaintThread==null ) {
              entry.setDisabled();
              inpaintThread = new Thread(this);
              inpaintThread.start();
          }
     }

     private void PauseActionPerformed(ActionEvent evt) {
         if (inpaintThread != null) {
            Inpainter.halt = true;
         }
         /*if ( inpaintThread==null ) {
              entry.setDisabled();
              inpaintThread = new Thread(this);
              inpaintThread.start();
          }*/
     }
     /**
      * action performed for exit
      * @param evt
      */
     private void jMenuItem3ActionPerformed(ActionEvent evt)
     {
               System.exit(0);
     }
     
     class SymAction implements java.awt.event.ActionListener {
         public void actionPerformed(java.awt.event.ActionEvent event)
         {
             Object object = event.getSource();             
         }
     }

     /**
      * calls the method to start inpainting
      */
    public void run()
    {
        Inpainter.init((BufferedImage)entry.getImage(), (BufferedImage)entry.getImage(),fastInpaint);
    }

    /**
     * Method to communicate with the Inpainting module
     * @param toShow updated image
     */
    public void updateStats(BufferedImage toShow)
    {
        UpdateStats stats = new UpdateStats();
        stats.toShow = toShow;
        try {
            SwingUtilities.invokeAndWait(stats);
        } catch ( Exception e ) {
            JOptionPane.showMessageDialog(this,"Error: " + e,"Training",
            JOptionPane.ERROR_MESSAGE);
        }


        if (Inpainter.completed) {                               // Inpainting completes
            
            JOptionPane.showMessageDialog(this,
                                    "                      Inpainting is completed.","Inpainting",
                                    JOptionPane.PLAIN_MESSAGE);
        }
        if (Inpainter.completed || Inpainter.halt) {
            System.out.println ("Inpainting completed or halted");
            inpaintThread = null;
            Image tmg = createImage(((Image)toShow).getWidth(this),((Image)toShow).getHeight(this));
            Graphics tg = tmg.getGraphics();
            tg.drawImage((Image)toShow,0,0,null);
            entry.SavedImages.push(tmg);
            entry.setEnabled();
            entry.RedoImages.clear();
            fastInpaint = false;
        }
    }

    public class UpdateStats implements Runnable {

        BufferedImage toShow;

        public void run()
        {
            entry.showImage(toShow);
        }
    }
}

