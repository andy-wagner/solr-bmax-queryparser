package com.s24.search.solr.query.bmax;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.lucene.analysis.Analyzer;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.ExtendedDismaxQParser;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;

import com.google.common.base.Joiner;

/**
 * A query parser plugin that acts as a edismax parser but does some
 * preprocessing to the incoming query.
 * 
 * @author Shopping24 GmbH, Torsten Bøgh Köster (@tboeghk)
 */
public class PreprocessingEdismaxQParserPlugin extends QParserPlugin {
   private String queryParsingFieldType;

   // pre-loaded analyzers to use
   private Analyzer queryParsingAnalyzer;

   @Override
   public void init(@SuppressWarnings("rawtypes") NamedList args) {
      checkNotNull(args, "Pre-condition violated: args must not be null.");

      // mandatory
      queryParsingFieldType = checkNotNull((String) args.get("queryParsingFieldType"),
            "No queryParsingFieldType given. Aborting.");
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
      checkNotNull(req, "Pre-condition violated: req must not be null.");

      // get query parsers if not available
      if (queryParsingAnalyzer == null) {
         this.queryParsingAnalyzer = (queryParsingFieldType != null) ? req.getSchema()
               .getFieldTypeByName(queryParsingFieldType)
               .getQueryAnalyzer() : null;
      }
      checkNotNull(queryParsingAnalyzer, "Pre-condition violated: queryParsingAnalyzer must not be null.");

      // pre-process query string here
      // parse and replace querystring
      if (!"*:*".equals(qstr)) {
         qstr = Joiner.on(' ').join(Terms.collect(qstr, queryParsingAnalyzer));
      }

      return new ExtendedDismaxQParser(qstr, localParams, params, req);
   }
}
