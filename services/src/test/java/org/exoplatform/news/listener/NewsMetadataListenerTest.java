package org.exoplatform.news.listener;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.news.model.News;
import org.exoplatform.news.search.NewsIndexingServiceConnector;
import org.exoplatform.news.utils.NewsUtils;
import org.exoplatform.services.listener.Event;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.metadata.tag.TagService;
import org.exoplatform.social.metadata.tag.model.TagName;
import org.exoplatform.social.metadata.tag.model.TagObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class NewsMetadataListenerTest {

  private static final String USERNAME = "testuser";

  @Mock
  private IndexingService     indexingService;

  @Mock
  private SpaceService        spaceService;

  @Mock
  private IdentityManager     identityManager;

  @Mock
  private TagService          tagService;

  @Test
  public void testCreateNewsTagsWhenNewsSaved() throws Exception {
    NewsMetadataListener newsMetadataListener = new NewsMetadataListener(indexingService,
                                                                         spaceService,
                                                                         identityManager,
                                                                         tagService);
    String newsId = "newsId";
    String spaceId = "spaceId";
    String content = "Test #tag1 Test #tag2.";

    News news = mock(News.class);
    when(news.getId()).thenReturn(newsId);
    when(news.getSpaceId()).thenReturn(spaceId);
    when(news.getBody()).thenReturn(content);

    Event<String, News> event = mock(Event.class);
    when(event.getData()).thenReturn(news);
    when(event.getSource()).thenReturn(USERNAME);

    HashSet<TagName> contentTags = new HashSet<>(Arrays.asList(new TagName("tag1"), new TagName("tag2")));

    String spacePrettyName = "spacePrettyName";

    Space space = mock(Space.class);
    when(space.getPrettyName()).thenReturn(spacePrettyName);
    when(spaceService.getSpaceById(spaceId)).thenReturn(space);

    String spaceIdentityId = "200";
    Identity spaceIdentity = mock(Identity.class);
    when(spaceIdentity.getId()).thenReturn(spaceIdentityId);

    when(identityManager.getOrCreateSpaceIdentity(spacePrettyName)).thenReturn(spaceIdentity);

    String userIdentityId = "300";
    Identity userIdentity = mock(Identity.class);
    when(userIdentity.getId()).thenReturn(userIdentityId);
    when(identityManager.getOrCreateUserIdentity(USERNAME)).thenReturn(userIdentity);

    when(tagService.detectTagNames(content)).thenReturn(contentTags);

    newsMetadataListener.onEvent(event);

    verify(indexingService, times(1)).reindex(NewsIndexingServiceConnector.TYPE, newsId);
    verify(tagService, times(1)).saveTags(new TagObject(NewsUtils.NEWS_METADATA_OBJECT_TYPE,
                                                        news.getId(),
                                                        null),
                                          contentTags,
                                          Long.parseLong(spaceIdentityId),
                                          Long.parseLong(userIdentityId));
  }

}
