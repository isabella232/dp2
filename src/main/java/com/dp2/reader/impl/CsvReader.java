package com.dp2.reader.impl;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import com.dp2.reader.AbstractReader;
import com.dp2.util.IOUtil;
import com.dp2.util.ReaderUtil;
import com.dp2.util.Types;

/**
 * CSV读取
 *
 * @author 6tail
 *
 */
public class CsvReader extends AbstractReader implements Closeable{
  /** 回车符 */
  public static String CR = "\r";
  /** 换行符 */
  public static String LF = "\n";
  /** 列间隔符 */
  public static final String SPACE = ",";
  /** 双引号 */
  public static final String QUOTE = "\"";
  private BufferedReader reader;
  /** 缓存 */
  private StringBuffer buffer = new StringBuffer();
  /** 标识数据内容是否包含在引号之间 */
  private boolean quoted = false;

  public CsvReader(File file){
    super(file);
  }

  /**
   * close
   *
   */
  public void close(){
    IOUtil.closeQuietly(reader);
  }

  /**
   * 按间隔符拆分字符串
   *
   * @param s 字符串
   * @param sp 间隔符
   * @return 拆分后的列表
   */
  private List<String> split(String s,String sp){
    List<String> l = new ArrayList<String>();
    String r = s;
    while(r.contains(sp)){
      int space = r.indexOf(sp);
      l.add(r.substring(0,space));
      r = r.substring(space+sp.length());
    }
    l.add(r);
    return l;
  }

  /**
   * 按照CSV格式规范将拆散的本来是一列的数据合并
   *
   * @param segments 拆散的列
   * @return 合并后的列
   */
  private List<String> combine(List<String> segments){
    List<String> l = new ArrayList<String>();
    for(String o:segments){
      String t = o.replace(QUOTE+QUOTE,"");
      if(t.startsWith(QUOTE)){
        if(!quoted){
          quoted = true;
          buffer.append(o);
          if(t.endsWith(QUOTE)){
            if(!t.equals(QUOTE)){
              l.add(buffer.toString());
              buffer.delete(0,buffer.length());
              quoted = false;
            }
          }
        }else{
          if(t.equals(QUOTE)){
            buffer.append(SPACE);
            buffer.append(o);
            l.add(buffer.toString());
            buffer.delete(0,buffer.length());
            quoted = false;
          }else{
            l.add(buffer.toString());
            buffer.delete(0,buffer.length());
            buffer.append(o);
            quoted = true;
          }
        }
      }else if(t.endsWith(QUOTE)){
        if(quoted){
          buffer.append(SPACE);
          buffer.append(o);
          l.add(buffer.toString());
          buffer.delete(0,buffer.length());
          quoted = false;
        }else{
          l.add(o);
        }
      }else{
        if(quoted){
          buffer.append(SPACE);
          buffer.append(o);
        }else{
          l.add(o);
        }
      }
    }
    return l;
  }

  protected String readLine(){
    try{
      return reader.readLine();
    }catch(IOException e){
      e.printStackTrace();
      close();
    }
    return null;
  }

  /**
   * 读取下一行
   *
   * @return 一行数据，如果没有下一行，返回null
   */
  public List<String> nextLine(){
    buffer.delete(0,buffer.length());
    quoted = false;
    String line = readLine();
    if(null==line){
      return null;
    }
    List<String> l = new ArrayList<String>();
    StringBuilder r = new StringBuilder(line);
    if(!r.toString().contains(QUOTE)){
      l.addAll(split(r.toString(),SPACE));
    }else{
      String t = r.toString().replace(QUOTE+QUOTE,"");
      int count = t.length()-t.replace(QUOTE,"").length();
      while(count%2==1){
        String nextLine = readLine();
        if(null==nextLine){
          nextLine = "\"";
        }
        r.append(CR).append(LF).append(nextLine);
        String nt = nextLine.replace(QUOTE+QUOTE,"");
        int len = nt.length()-nt.replace(QUOTE,"").length();
        count += len;
      }
      List<String> segments = split(r.toString(),SPACE);
      l.addAll(combine(segments));
    }
    List<String> cols = new ArrayList<String>();
    for(String col:l){
      if(col.equals(QUOTE)){
        col = "";
      }else if(col.equals(QUOTE+QUOTE)){
        col = "";
      }else if(col.startsWith(QUOTE)&&col.endsWith(QUOTE)){
        col = col.replace(QUOTE+QUOTE,QUOTE);
        col = col.substring(QUOTE.length());
        col = col.substring(0,col.length()-QUOTE.length());
      }
      cols.add(col);
    }
    return cols;
  }

  public void load() throws IOException{
    reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), ReaderUtil.getCharset(file)));
    stop = false;
    quoted = false;
    buffer.delete(0,buffer.length());
  }

  @Override
  public boolean support(){
    return true;
  }

  public String type(){
    return Types.CSV;
  }
}
