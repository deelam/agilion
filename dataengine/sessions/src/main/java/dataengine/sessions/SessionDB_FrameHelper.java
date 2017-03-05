package dataengine.sessions;

import static net.deelam.graph.GrafTxn.tryOn;

import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;

import com.google.common.base.CharMatcher;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FramedTransactionalGraph;

import dataengine.sessions.frames.BaseFrame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public final class SessionDB_FrameHelper {

  static Comparator<? super BaseFrame> createdTimeComparator =
      (f1, f2) -> f1.getCreatedDate().compareTo(f2.getCreatedDate());

  private final FramedTransactionalGraph<TransactionalGraph> graph;

  boolean hasFrame(String id, String typeValue) {
    return tryOn(graph, graph -> {
      Vertex vf = graph.getVertex(id);
      if (vf == null)
        return false;
      return typeValue.equals(vf.getProperty(BaseFrame.TYPE_KEY));
    });
  }

  boolean hasVertexFrame(String id) {
    return tryOn(graph, graph -> {
      Vertex vf = graph.getVertex(id);
      return (vf != null);
    });
  }

  <T> T getVertexFrame(String id, Class<T> clazz) {
    return tryOn(graph, graph -> {
      T vf = graph.getVertex(id, clazz);
      if (vf == null)
        throw new IllegalArgumentException("Node does not exists with id=" + id);
      return vf;
    });
  }

  static String checkLabel(String label) {
    if (label.contains("\n"))
      throw new IllegalArgumentException("Label cannot have carriage returns: "+label);
    if (label.contains("\t"))
      throw new IllegalArgumentException("Label cannot have tabs: "+label);
    if (label.length() > 255)
      throw new IllegalArgumentException("Label must be less than 255 characters! Got "+label.length());
    if(!CharMatcher.ascii().matchesAllOf(label))
      throw new IllegalArgumentException("Label cannot have non-ASCII characters: "+label);
    return label;
  }

  @SuppressWarnings("unchecked")
  static void saveMapAsProperties(Map<?, ?> map, Vertex v, String propPrefix) {
    for (Map.Entry<String, Object> e : ((Map<String, Object>) map).entrySet()) {
      setVertexProperty(v, propPrefix, e.getKey(), e.getValue());
    }
  }

  static void setVertexProperty(Vertex v, String propPrefix, String keySuffix, Object val) {
    if (val instanceof String ||
        val instanceof Number ||
        val instanceof Boolean){
      v.setProperty(propPrefix + keySuffix, val);
    } else if(val instanceof URI){
      v.setProperty(propPrefix + keySuffix, val.toString());
    }else if(val==null){
      log.warn("Ignoring null value for property {}", propPrefix + keySuffix);
    } else {
      log.warn("Storing as string -- don't know how to store value as property {}: {} {}", propPrefix + keySuffix,
          val.getClass(), val);
      v.setProperty(propPrefix + keySuffix, val.toString());
    }
  }

  static Map<String, Object> loadPropertiesAsMap(Vertex v, String propPrefix) {
    int ppIndex = propPrefix.length();
    Map<String, Object> map = new HashMap<>();
    for (String key : v.getPropertyKeys()) {
      if (key.startsWith(propPrefix)) {
        map.put(key.substring(ppIndex), v.getProperty(key));
      }
    }
    return map;
  }

  public static OffsetDateTime toOffsetDateTime(DateTime createdTime) {
    return OffsetDateTime.ofInstant(Instant.ofEpochMilli(createdTime.getMillis()),
        ZoneOffset.UTC);
  }

  public static DateTime toJodaDateTime(OffsetDateTime offsetDateTime) {
    return new DateTime(offsetDateTime.toInstant().toEpochMilli());
  }

}
