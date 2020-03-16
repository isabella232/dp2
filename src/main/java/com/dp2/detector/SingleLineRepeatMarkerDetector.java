package com.dp2.detector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.dp2.marker.Marker;
import com.dp2.marker.RepeatedMarker;
import com.dp2.reader.IReader;
import com.dp2.reader.ReaderFactory;

/**
 * 单行的重复节点标记探测器
 *
 * @author 6tail
 *
 */
public class SingleLineRepeatMarkerDetector{

  public static List<Marker> detect(File file) throws IOException{
    IReader reader = ReaderFactory.getReader(file);
    reader.load();
    int startRow = 0;
    int width = 0;
    int fillWidth = 0;
    int row = 0;
    List<String> line;
    while(null!=(line = reader.nextLine())){
      int w = line.size();
      if(w>width){
        width = w;
        startRow = row;
        fillWidth = 0;
      }
      if(w==width){
        int fill = 0;
        for(String value:line){
          if(value.length()<1){
            continue;
          }
          fill++;
        }
        if(fill>fillWidth){
          fillWidth = fill;
          startRow = row;
        }
      }
      row++;
    }
    List<Marker> l = new ArrayList<Marker>();
    if(width>0){
      Marker head = new Marker("head",startRow,0);
      head.setWidth(width);
      head.setHeight(1);
      for(int i = 0;i<width;i++){
        Marker child = new Marker(i+"",0,i);
        head.addChild(child);
      }
      l.add(head);
      RepeatedMarker body = new RepeatedMarker("body",startRow+1,0);
      body.setWidth(width);
      body.setHeight(1);
      for(int i = 0;i<width;i++){
        Marker child = new Marker(i+"",0,i);
        body.addChild(child);
      }
      l.add(body);
    }
    return l;
  }
}
