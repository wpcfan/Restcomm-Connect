/*
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.mobicents.servlet.sip.restcomm.dao.mybatis;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.joda.time.DateTime;
import org.mobicents.servlet.sip.restcomm.Sid;
import org.mobicents.servlet.sip.restcomm.Transcription;
import org.mobicents.servlet.sip.restcomm.annotations.concurrency.ThreadSafe;
import org.mobicents.servlet.sip.restcomm.dao.TranscriptionsDao;

/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
@ThreadSafe public final class MybatisTranscriptionsDao implements TranscriptionsDao {
  private static final String namespace = "org.mobicents.servlet.sip.restcomm.dao.TranscriptionsDao.";
  private final SqlSessionFactory sessions;
  
  public MybatisTranscriptionsDao(final SqlSessionFactory sessions) {
    super();
    this.sessions = sessions;
  }
  
  @Override public void addTranscription(final Transcription transcription) {
    final SqlSession session = sessions.openSession();
    try {
      session.insert(namespace + "addTranscription", toMap(transcription));
    } finally {
      session.close();
    }
  }

  @Override public Transcription getTranscription(final Sid sid) {
    return getTranscription(namespace + "getTranscription", sid);
  }

  @Override public Transcription getTranscriptionByRecording(final Sid recordingSid) {
    return getTranscription(namespace + "getTranscriptionByRecording", recordingSid);
  }
  
  private Transcription getTranscription(final String selector, final Sid sid) {
    final SqlSession session = sessions.openSession();
    try {
      @SuppressWarnings("unchecked")
	  final Map<String, Object> result = (Map<String, Object>)session.selectOne(selector, sid.toString());
      if(result != null) {
        return toTranscription(result);
      } else {
        return null;
      }
    } finally {
      session.close();
    }
  }

  @Override public List<Transcription> getTranscriptions(final Sid accountSid) {
    final SqlSession session = sessions.openSession();
    try {
      @SuppressWarnings("unchecked")
      final List<Map<String, Object>> results = (List<Map<String, Object>>)session.selectList(namespace + "getTranscriptions", accountSid.toString());
      final List<Transcription> transcriptions = new ArrayList<Transcription>();
      if(results != null && !results.isEmpty()) {
        for(final Map<String, Object> result : results) {
          transcriptions.add(toTranscription(result));
        }
      }
      return transcriptions;
    } finally {
      session.close();
    }
  }

  @Override public void removeTranscription(final Sid sid) {
    removeTranscriptions(namespace + "removeTranscription", sid);
  }

  @Override public void removeTranscriptions(final Sid accountSid) {
    removeTranscriptions(namespace + "removeTranscriptions", accountSid);
  }
  
  private void removeTranscriptions(final String selector, final Sid sid) {
    final SqlSession session = sessions.openSession();
    try {
      session.delete(selector, sid.toString());
    } finally {
      session.close();
    }
  }
  
  private Map<String, Object> toMap(final Transcription transcription) {
    final Map<String, Object> map = new HashMap<String, Object>();
    map.put("sid", transcription.getSid().toString());
    map.put("date_created", transcription.getDateCreated().toDate());
    map.put("date_updated", transcription.getDateUpdated().toDate());
    map.put("account_sid", transcription.getAccountSid().toString());
    map.put("status", transcription.getStatus());
    map.put("recording_sid", transcription.getRecordingSid().toString());
    map.put("duration", transcription.getDuration());
    map.put("transcription_text", transcription.getTranscriptionText());
    map.put("price", transcription.getPrice().toString());
    map.put("uri", transcription.getUri().toString());
    return map;
  }
  
  private Transcription toTranscription(final Map<String, Object> map) {
    final Sid sid = new Sid((String)map.get("sid"));
    final DateTime dateCreated = new DateTime((Date)map.get("date_created"));
    final DateTime dateUpdated = new DateTime((Date)map.get("date_updated"));
    final Sid accountSid = new Sid((String)map.get("account_sid"));
    final String status = (String)map.get("status");
    final Sid recordingSid = new Sid((String)map.get("recording_sid"));
    final Integer duration = (Integer)map.get("duration");
    final String transcriptionText = (String)map.get("transcription_text");
    final BigDecimal price = new BigDecimal((String)map.get("price"));
    final URI uri = URI.create((String)map.get("uri"));
    return new Transcription(sid, dateCreated, dateUpdated, accountSid, status, recordingSid,
        duration, transcriptionText, price, uri);
  }
}
