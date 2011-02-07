/*
 * Licensed to the Sakai Foundation (SF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.sakaiproject.nakamura.presence.search;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONWriter;
import org.osgi.framework.Constants;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.api.presence.PresenceService;
import org.sakaiproject.nakamura.api.presence.PresenceUtils;
import org.sakaiproject.nakamura.api.profile.LiteProfileService;
import org.sakaiproject.nakamura.api.search.SearchConstants;
import org.sakaiproject.nakamura.api.search.solr.Result;
import org.sakaiproject.nakamura.api.search.solr.SolrSearchException;
import org.sakaiproject.nakamura.api.search.solr.SolrSearchResultProcessor;
import org.sakaiproject.nakamura.api.search.solr.SolrSearchResultSet;
import org.sakaiproject.nakamura.api.search.solr.SolrSearchServiceFactory;
import org.sakaiproject.nakamura.util.ExtendedJSONWriter;

/**
 * Search result processor to write out profile information when search returns home nodes
 * (sakai/user-home). This result processor should live in the user bundle but at the time
 * of this writing, moving to that bundle creates a cyclical dependency of:<br/>
 * search -&gt; personal -&gt; user -&gt; search
 */
@Component
@Service
@Properties({
  @Property(name = Constants.SERVICE_VENDOR, value = "The Sakai Foundation"),
  @Property(name = SearchConstants.REG_PROCESSOR_NAMES, value = "Profile")
})
public class ProfileNodeSearchResultProcessor implements SolrSearchResultProcessor {
  @Reference
  private SolrSearchServiceFactory searchServiceFactory;

  @Reference
  private LiteProfileService profileService;

  @Reference
  private PresenceService presenceService;

  public ProfileNodeSearchResultProcessor() {
  }

  ProfileNodeSearchResultProcessor(SolrSearchServiceFactory searchServiceFactory,
      LiteProfileService profileService, PresenceService presenceService) {
    if (searchServiceFactory == null || profileService == null || presenceService == null) {
      throw new IllegalArgumentException(
          "SearchServiceFactory, ProfileService and PresenceService must be set when not using as a component");
    }
    this.searchServiceFactory = searchServiceFactory;
    this.presenceService = presenceService;
    this.profileService = profileService;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.sakaiproject.nakamura.api.search.SearchResultProcessor#getSearchResultSet(org.apache.sling.api.SlingHttpServletRequest,
   *      javax.jcr.query.Query)
   */
  public SolrSearchResultSet getSearchResultSet(SlingHttpServletRequest request,
      String queryString) throws SolrSearchException {
    // return the result set
    return searchServiceFactory.getSearchResultSet(request, queryString);
  }

  /**
   * {@inheritDoc}
   *
   * @see org.sakaiproject.nakamura.search.processors.SearchResultProcessor#writeNode(org.apache.sling.api.SlingHttpServletRequest,
   *      org.apache.sling.commons.json.io.JSONWriter,
   *      org.sakaiproject.nakamura.api.search.Aggregator, javax.jcr.query.Row)
   */
  public void writeResult(SlingHttpServletRequest request, JSONWriter write, Result result) throws JSONException {
    String path = result.getPath();
    ResourceResolver resolver = request.getResourceResolver();
    Resource resource = resolver.getResource(path);
    write.object();
    if (resource != null) {
      Content content = resource.adaptTo(Content.class);
      ValueMap map = profileService.getProfileMap(content);
      ((ExtendedJSONWriter) write).valueMapInternals(map);
    }

    // If this is a User Profile, then include Presence data.
    String userId = (String) result.getFirstValue(User.NAME_FIELD);
    if (userId != null) {
      PresenceUtils.makePresenceJSON(write, userId, presenceService, true);
    }
    write.endObject();
  }
}
