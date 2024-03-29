/*
 * Copyright (C) 2024 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.news.service.impl;

import static org.exoplatform.news.service.impl.NewsServiceImplV2.NEWS_ARTICLES_ROOT_NOTE_PAGE_NAME;
import static org.exoplatform.news.service.impl.NewsServiceImplV2.NEWS_SUMMARY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.news.filter.NewsFilter;
import org.exoplatform.news.model.News;
import org.exoplatform.news.model.NewsDraftObject;
import org.exoplatform.news.service.NewsService;
import org.exoplatform.news.service.NewsTargetingService;
import org.exoplatform.news.utils.NewsUtils;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataObject;
import org.exoplatform.upload.UploadService;
import org.exoplatform.wiki.model.DraftPage;
import org.exoplatform.wiki.model.Wiki;
import org.exoplatform.wiki.service.NoteService;
import org.exoplatform.wiki.service.WikiService;

@RunWith(MockitoJUnitRunner.Silent.class)
public class NewsServiceImplV2Test {

  @Mock
  private SpaceService                               spaceService;

  @Mock
  private NoteService                                noteService;

  @Mock
  private MetadataService                            metadataService;

  @Mock
  private FileService                                fileService;

  @Mock
  private UploadService                              uploadService;

  @Mock
  private IndexingService                            indexingService;

  @Mock
  NewsTargetingService                               newsTargetingService;

  @Mock
  IdentityManager                                    identityManager;

  @Mock
  ActivityManager                                    activityManager;

  @Mock
  WikiService                                        wikiService;

  private NewsService                                newsService;

  private UserACL                                    userACL;

  private static final MockedStatic<CommonsUtils>    COMMONS_UTILS    = mockStatic(CommonsUtils.class);

  private static final MockedStatic<PortalContainer> PORTAL_CONTAINER = mockStatic(PortalContainer.class);

  private static final MockedStatic<NewsUtils>       NEWS_UTILS       = mockStatic(NewsUtils.class);

  @Before
  public void setUp() {
    userACL = CommonsUtils.getService(UserACL.class);
    this.newsService = new NewsServiceImplV2(spaceService,
                                             noteService,
                                             metadataService,
                                             fileService,
                                             newsTargetingService,
                                             indexingService,
                                             identityManager,
                                             userACL,
                                             activityManager,
                                             wikiService,
                                             uploadService);
  }

  @AfterClass
  public static void afterRunBare() throws Exception { // NOSONAR
    COMMONS_UTILS.close();
    PORTAL_CONTAINER.close();
    NEWS_UTILS.close();
  }

  @Test
  public void testCreateDraftArticle() throws Exception {

    // Given
    News draftArticle = new News();
    draftArticle.setAuthor("john");
    draftArticle.setTitle("draft article for new page");
    draftArticle.setSummary("draft article summary for new page");
    draftArticle.setBody("draft body");
    draftArticle.setPublicationState("draft");

    Space space = mock(Space.class);
    when(spaceService.getSpaceById(draftArticle.getSpaceId())).thenReturn(space);
    when(spaceService.getSpaceByGroupId(anyString())).thenReturn(space);
    when(space.getGroupId()).thenReturn("/space/groupId");
    when(space.getId()).thenReturn("1");

    DraftPage draftPage = new DraftPage();
    draftPage.setContent(draftArticle.getBody());
    draftPage.setTitle(draftArticle.getTitle());
    draftPage.setId("1");
    draftPage.setAuthor("john");

    Identity identity = mock(Identity.class);
    when(identity.getUserId()).thenReturn("john");
    when(spaceService.getSpaceById(any())).thenReturn(space);
    when(spaceService.isSuperManager(anyString())).thenReturn(true);
    Wiki wiki = mock(Wiki.class);
    when(wikiService.getWikiByTypeAndOwner(anyString(), anyString())).thenReturn(wiki);
    org.exoplatform.wiki.model.Page rootPage = mock(org.exoplatform.wiki.model.Page.class);
    when(rootPage.getName()).thenReturn(NEWS_ARTICLES_ROOT_NOTE_PAGE_NAME);

    // When
    News savedDraftArticle = newsService.createNews(draftArticle, identity);

    // Then
    assertNull(savedDraftArticle);

    // Given
    when(noteService.getNoteOfNoteBookByName("group",
                                             space.getGroupId(),
                                             NEWS_ARTICLES_ROOT_NOTE_PAGE_NAME)).thenReturn(rootPage);
    when(noteService.createDraftForNewPage(any(DraftPage.class), anyLong())).thenReturn(draftPage);
    when(rootPage.getId()).thenReturn("1");
    org.exoplatform.social.core.identity.model.Identity identity1 =
                                                                  mock(org.exoplatform.social.core.identity.model.Identity.class);
    when(identityManager.getOrCreateUserIdentity(anyString())).thenReturn(identity1);
    when(identity1.getId()).thenReturn("1");
    NEWS_UTILS.when(() -> NewsUtils.canPublishNews(anyString(), any(Identity.class))).thenReturn(true);
    when(spaceService.canRedactOnSpace(any(Space.class), any(Identity.class))).thenReturn(true);

    // When
    savedDraftArticle = newsService.createNews(draftArticle, identity);

    // Then
    assertNotNull(savedDraftArticle);
    verify(metadataService, times(1)).createMetadataItem(any(NewsDraftObject.class),
                                                         any(MetadataKey.class),
                                                         any(Map.class),
                                                         anyLong());
    assertNotNull(savedDraftArticle.getId());
    assertEquals(draftPage.getId(), savedDraftArticle.getId());
    assertEquals(draftPage.getTitle(), savedDraftArticle.getTitle());
    assertEquals(draftPage.getContent(), savedDraftArticle.getBody());
    assertEquals(draftPage.getAuthor(), savedDraftArticle.getAuthor());
  }

  @Test
  public void testGetDraftArticleById() throws Exception {

    // Given
    DraftPage draftPage = new DraftPage();
    draftPage.setContent("draft body");
    draftPage.setTitle("draft article for new page");
    draftPage.setId("1");
    draftPage.setAuthor("john");
    draftPage.setWikiOwner("/space/groupId");

    Space space = mock(Space.class);
    when(spaceService.getSpaceByGroupId(anyString())).thenReturn(space);
    when(space.getGroupId()).thenReturn("/space/groupId");
    when(space.getAvatarUrl()).thenReturn("space/avatar/url");
    when(space.getDisplayName()).thenReturn("spaceDisplayName");
    when(space.getVisibility()).thenReturn("public");
    when(space.getId()).thenReturn("1");
    when(spaceService.getSpaceById("1")).thenReturn(space);
    when(spaceService.isSuperManager(anyString())).thenReturn(true);

    when(noteService.getDraftNoteById(anyString(), anyString())).thenReturn(draftPage);
    MetadataItem metadataItem = mock(MetadataItem.class);
    List<MetadataItem> metadataItems = new ArrayList<>();
    metadataItems.add(metadataItem);
    when(metadataService.getMetadataItemsByMetadataAndObject(any(MetadataKey.class),
                                                             any(MetadataObject.class))).thenReturn(metadataItems);
    Map<String, String> properties = new HashMap<>();
    properties.put(NEWS_SUMMARY, draftPage.getContent());
    when(metadataItem.getProperties()).thenReturn(properties);
    PORTAL_CONTAINER.when(() -> PortalContainer.getCurrentPortalContainerName()).thenReturn("portal");
    COMMONS_UTILS.when(() -> CommonsUtils.getCurrentPortalOwner()).thenReturn("dw");
    Identity identity = mock(Identity.class);
    when(identity.getUserId()).thenReturn("john");
    when(activityManager.getActivity(nullable(String.class))).thenReturn(null);
    when(newsTargetingService.getTargetsByNewsId(anyString())).thenReturn(null);

    // When
    News news = newsService.getNewsById("1", identity, false, NewsUtils.NewsObjectType.DRAFT.name().toLowerCase());

    // Then
    assertNotNull(news);
    assertEquals(draftPage.getId(), news.getId());
    assertEquals(draftPage.getAuthor(), news.getAuthor());
    assertEquals(draftPage.getContent(), news.getBody());
    assertEquals("draft", news.getPublicationState());
    assertEquals(space.getDisplayName(), news.getSpaceDisplayName());
    assertEquals(space.getAvatarUrl(), news.getSpaceAvatarUrl());
    assertEquals("/portal/dw/news/detail?newsId=1&type=draft", news.getUrl());
  }

  @Test
  public void testUpdateDraftArticle() throws Exception {

    // Given
    DraftPage draftPage = new DraftPage();
    draftPage.setContent("draft body");
    draftPage.setTitle("draft article for new page");
    draftPage.setId("1");
    draftPage.setAuthor("john");
    draftPage.setWikiOwner("/space/groupId");

    Space space = mock(Space.class);
    when(space.getId()).thenReturn("1");
    when(space.getGroupId()).thenReturn("/space/groupId");
    when(space.getAvatarUrl()).thenReturn("space/avatar/url");
    when(space.getDisplayName()).thenReturn("spaceDisplayName");
    when(space.getVisibility()).thenReturn("public");
    when(spaceService.isSuperManager(anyString())).thenReturn(true);
    when(spaceService.getSpaceById(any())).thenReturn(space);
    when(spaceService.getSpaceByGroupId(anyString())).thenReturn(space);

    when(noteService.getDraftNoteById(anyString(), anyString())).thenReturn(draftPage);
    MetadataItem metadataItem = mock(MetadataItem.class);
    List<MetadataItem> metadataItems = new ArrayList<>();
    metadataItems.add(metadataItem);
    when(metadataService.getMetadataItemsByMetadataAndObject(any(MetadataKey.class),
                                                             any(MetadataObject.class))).thenReturn(metadataItems);
    Map<String, String> properties = new HashMap<>();
    when(metadataItem.getProperties()).thenReturn(properties);
    PORTAL_CONTAINER.when(() -> PortalContainer.getCurrentPortalContainerName()).thenReturn("portal");
    COMMONS_UTILS.when(() -> CommonsUtils.getCurrentPortalOwner()).thenReturn("dw");
    Identity identity = mock(Identity.class);
    when(identity.getUserId()).thenReturn("john");
    when(activityManager.getActivity(nullable(String.class))).thenReturn(null);
    when(newsTargetingService.getTargetsByNewsId(anyString())).thenReturn(null);
    org.exoplatform.wiki.model.Page rootPage = mock(org.exoplatform.wiki.model.Page.class);
    when(rootPage.getName()).thenReturn(NEWS_ARTICLES_ROOT_NOTE_PAGE_NAME);
    when(noteService.getDraftNoteById(anyString(), anyString())).thenReturn(draftPage);
    NEWS_UTILS.when(() -> NewsUtils.getUserIdentity(anyString())).thenReturn(identity);
    News news = new News();
    news.setAuthor("john");
    news.setTitle("new draft title");
    news.setBody("draft body");
    news.setId("1");
    news.setPublicationState("draft");
    news.setSpaceId(space.getId());
    news.setSummary("news summary");

    DraftPage expecteddraftPage = new DraftPage();
    expecteddraftPage.setTitle(news.getTitle());
    expecteddraftPage.setAuthor(news.getAuthor());
    expecteddraftPage.setContent(news.getBody());
    expecteddraftPage.setId(news.getId());
    expecteddraftPage.setWikiOwner("/space/groupId");

    // When, Then
    assertThrows(IllegalArgumentException.class, () -> newsService.updateNews(news, "john", false, false, "draft"));

    // Given
    when(spaceService.canRedactOnSpace(space, identity)).thenReturn(true);
    org.exoplatform.social.core.identity.model.Identity identity1 =
                                                                  mock(org.exoplatform.social.core.identity.model.Identity.class);
    when(identityManager.getOrCreateUserIdentity(anyString())).thenReturn(identity1);
    when(identity1.getId()).thenReturn("1");
    when(noteService.updateDraftForNewPage(any(DraftPage.class), anyLong())).thenReturn(expecteddraftPage);

    // When
    newsService.updateNews(news, "john", false, false, "draft");

    // Then
    verify(noteService, times(1)).updateDraftForNewPage(eq(expecteddraftPage), anyLong());
    verify(metadataService, times(1)).updateMetadataItem(any(MetadataItem.class), anyLong());
  }

  @Test
  public void testDeleteDraftArticle() throws Exception {

    // Given
    DraftPage draftPage = new DraftPage();
    draftPage.setContent("draft body");
    draftPage.setTitle("draft article for new page");
    draftPage.setId("1");
    draftPage.setAuthor("john");
    draftPage.setWikiOwner("/space/groupId");

    Space space = mock(Space.class);
    when(space.getId()).thenReturn("1");
    when(space.getGroupId()).thenReturn("/space/groupId");
    when(space.getAvatarUrl()).thenReturn("space/avatar/url");
    when(space.getDisplayName()).thenReturn("spaceDisplayName");
    when(space.getVisibility()).thenReturn("public");
    when(spaceService.isSuperManager(anyString())).thenReturn(true);
    when(spaceService.getSpaceById(any())).thenReturn(space);
    when(spaceService.getSpaceByGroupId(anyString())).thenReturn(space);

    when(noteService.getDraftNoteById(anyString(), anyString())).thenReturn(draftPage);
    MetadataItem metadataItem = mock(MetadataItem.class);
    List<MetadataItem> metadataItems = new ArrayList<>();
    metadataItems.add(metadataItem);
    when(metadataService.getMetadataItemsByMetadataAndObject(any(MetadataKey.class),
                                                             any(MetadataObject.class))).thenReturn(metadataItems);
    Map<String, String> properties = new HashMap<>();
    when(metadataItem.getProperties()).thenReturn(properties);
    PORTAL_CONTAINER.when(() -> PortalContainer.getCurrentPortalContainerName()).thenReturn("portal");
    COMMONS_UTILS.when(() -> CommonsUtils.getCurrentPortalOwner()).thenReturn("dw");
    Identity identity = mock(Identity.class);
    when(identity.getUserId()).thenReturn("john");
    when(activityManager.getActivity(nullable(String.class))).thenReturn(null);
    when(newsTargetingService.getTargetsByNewsId(anyString())).thenReturn(null);
    org.exoplatform.wiki.model.Page rootPage = mock(org.exoplatform.wiki.model.Page.class);
    when(rootPage.getName()).thenReturn(NEWS_ARTICLES_ROOT_NOTE_PAGE_NAME);
    when(noteService.getDraftNoteById(anyString(), anyString())).thenReturn(draftPage);
    NEWS_UTILS.when(() -> NewsUtils.getUserIdentity(anyString())).thenReturn(identity);
    when(spaceService.canRedactOnSpace(space, identity)).thenReturn(true);

    // When
    newsService.deleteNews(draftPage.getId(), identity, true);

    // Then
    verify(noteService, times(1)).removeDraftById(draftPage.getId());
    verify(metadataService, times(1)).deleteMetadataItem(any(Long.class), anyBoolean());
  }

  @Test
  public void testGetDraftArticles() throws Exception {

    // Given
    DraftPage draftPage = new DraftPage();
    draftPage.setContent("draft body");
    draftPage.setTitle("draft article for new page");
    draftPage.setId("1");
    draftPage.setAuthor("john");
    draftPage.setWikiOwner("/space/groupId");

    Space space1 = mock(Space.class);
    when(space1.getId()).thenReturn("1");
    when(spaceService.getSpaceByGroupId(anyString())).thenReturn(space1);
    when(space1.getGroupId()).thenReturn("/space/groupId");
    when(space1.getAvatarUrl()).thenReturn("space/avatar/url");
    when(space1.getDisplayName()).thenReturn("spaceDisplayName");
    when(space1.getVisibility()).thenReturn("public");
    when(spaceService.getSpaceById("1")).thenReturn(space1);
    when(spaceService.isSuperManager(anyString())).thenReturn(true);
    when(noteService.getDraftNoteById(anyString(), anyString())).thenReturn(draftPage);

    Map<String, String> properties = new HashMap<>();
    properties.put(NEWS_SUMMARY, draftPage.getContent());
    MetadataItem metadataItem = mock(MetadataItem.class);
    List<MetadataItem> metadataItems = Arrays.asList(metadataItem);
    when(metadataItem.getObjectId()).thenReturn("1");
    when(metadataItem.getProperties()).thenReturn(properties);
    when(metadataService.getMetadataItemsByMetadataAndObject(any(MetadataKey.class),
                                                             any(MetadataObject.class))).thenReturn(metadataItems);
    PORTAL_CONTAINER.when(() -> PortalContainer.getCurrentPortalContainerName()).thenReturn("portal");
    COMMONS_UTILS.when(() -> CommonsUtils.getCurrentPortalOwner()).thenReturn("dw");
    Identity identity = mock(Identity.class);
    when(identity.getUserId()).thenReturn("john");
    List<Space> allowedDraftNewsSpaces = Arrays.asList(space1);
    NEWS_UTILS.when(() -> NewsUtils.getAllowedDraftNewsSpaces(identity)).thenReturn(allowedDraftNewsSpaces);
    when(metadataService.getMetadataItemsByMetadataNameAndTypeAndObjectAndSpaceIds(anyString(),
                                                                                   anyString(),
                                                                                   anyString(),
                                                                                   anyList(),
                                                                                   anyLong(),
                                                                                   anyLong())).thenReturn(metadataItems);

    when(activityManager.getActivity(nullable(String.class))).thenReturn(null);
    when(newsTargetingService.getTargetsByNewsId(anyString())).thenReturn(null);

    // When
    NewsFilter newsFilter = new NewsFilter();
    newsFilter.setDraftNews(true);
    newsFilter.setOffset(0);
    newsFilter.setLimit(10);
    List<News> newsList = newsService.getNews(newsFilter, identity);

    // Then
    assertNotNull(newsList);
    assertEquals(newsList.size(), 1);
  }
}
