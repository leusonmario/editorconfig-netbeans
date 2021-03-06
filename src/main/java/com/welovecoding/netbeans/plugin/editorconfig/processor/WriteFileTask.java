/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.welovecoding.netbeans.plugin.editorconfig.processor;

import com.welovecoding.netbeans.plugin.editorconfig.model.FileAttributeName;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.queries.FileEncodingQuery;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Michael Koppen
 */
public abstract class WriteFileTask implements Runnable {

  private static final Logger LOG = Logger.getLogger(WriteFileTask.class.getName());

  private final FileObject fo;
  private final Charset cs;

  public WriteFileTask(FileObject fo, Charset cs) {
    this.fo = fo;
    this.cs = cs;
  }

  public WriteFileTask(FileObject fo) {
    this.fo = fo;
    this.cs = FileEncodingQuery.getEncoding(fo);
  }

  @Override
  public void run() {
    FileLock lock = FileLock.NONE;
    try {
      try (OutputStream outputStream = fo.getOutputStream(lock); OutputStreamWriter writer = new OutputStreamWriter(outputStream, cs)) {
        // #####################
        apply(writer);
        // #####################
        setFileAttribute(fo, FileAttributeName.ENCODING, cs.name());
        writer.flush();
        outputStream.flush();
      }
    } catch (IOException ex) {
      Exceptions.printStackTrace(ex);
    } finally {
      lock.releaseLock();
    }
  }

  private void setFileAttribute(FileObject fo, String key, String value) {
    try {
      fo.setAttribute(key, value);
    } catch (IOException ex) {
      LOG.log(Level.SEVERE, "Error setting file attribute \"{0}\" with value \"{1}\" for {2}. {3}",
              new Object[]{
                key,
                value,
                fo.getPath(),
                ex.getMessage()
              });
    }
  }

  public abstract void apply(OutputStreamWriter writer);

}
