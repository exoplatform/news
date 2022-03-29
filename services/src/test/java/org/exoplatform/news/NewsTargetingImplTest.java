package org.exoplatform.news;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.news.model.News;
import org.exoplatform.news.model.NewsTargetObject;
import org.exoplatform.news.rest.NewsTargetingEntity;
import org.exoplatform.news.service.NewsTargetingService;
import org.exoplatform.news.service.impl.NewsTargetingServiceImpl;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.services.wcm.publication.PublicationDefaultStates;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.Metadata;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.*", "org.w3c.*", "javax.naming.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
@PrepareForTest({ ExoContainerContext.class })
public class NewsTargetingImplTest {

  @Mock
  MetadataService  metadataService;

  @Mock
  IdentityManager  identityManager;

  @Mock
  IdentityRegistry identityRegistry;

  @Test
  public void shouldReturnTargets() {
    // Given
    NewsTargetingServiceImpl newsTargetingService = new NewsTargetingServiceImpl(metadataService, identityManager);
    MetadataType metadataType = new MetadataType(4, "newsTarget");
    List<Metadata> newsTargets = new LinkedList<>();
    Metadata sliderNews = new Metadata();
    sliderNews.setName("sliderNews");
    sliderNews.setCreatedDate(100);
    HashMap<String, String> sliderNewsProperties = new HashMap<>();
    sliderNewsProperties.put("label", "slider news");
    sliderNews.setProperties(sliderNewsProperties);
    sliderNews.setId(1);
    newsTargets.add(sliderNews);

    Metadata newsTarget2 = new Metadata();
    newsTarget2.setName("latestNews");
    newsTarget2.setCreatedDate(200);
    HashMap<String, String> latestNewsProperties = new HashMap<>();
    latestNewsProperties.put("label", "latest news");
    newsTarget2.setProperties(latestNewsProperties);
    newsTarget2.setId(2);
    newsTargets.add(newsTarget2);

    when(metadataService.getMetadatas(metadataType.getName(),100)).thenReturn(newsTargets);

    // When
    List<NewsTargetingEntity> newsTargetingEntities = newsTargetingService.getTargets();

    // Then
    assertNotNull(newsTargetingEntities);
    assertEquals(2, newsTargetingEntities.size());
    assertEquals("sliderNews", newsTargetingEntities.get(0).getName());
    assertEquals("latestNews", newsTargetingEntities.get(1).getName());
  }

  @Test
  public void shouldNotReturnReferencedTargetsWhenReferencedIsFalse() throws IllegalAccessException {
    // Given
    NewsTargetingServiceImpl newsTargetingService = new NewsTargetingServiceImpl(metadataService, identityManager);
    org.exoplatform.services.security.Identity identity = new org.exoplatform.services.security.Identity("root");
    List<Metadata> newsTargets = new LinkedList<>();
    Metadata sliderNews = new Metadata();
    sliderNews.setName("sliderNews");
    sliderNews.setCreatedDate(100);
    HashMap<String, String> sliderNewsProperties = new HashMap<>();
    sliderNewsProperties.put("referenced", "false");
    sliderNews.setProperties(sliderNewsProperties);
    sliderNews.setId(1);
    newsTargets.add(sliderNews);
    List<MembershipEntry> memberships = new LinkedList<>();
    MembershipEntry membershipEntry = new MembershipEntry("/platform/web-contributors", "publisher");
    memberships.add(membershipEntry);
    identity.setMemberships(memberships);

    // When
    List<NewsTargetingEntity> newsTargetingEntities = newsTargetingService.getReferencedTargets(identity);

    // Then
    assertNotNull(newsTargetingEntities);
    assertEquals(0, newsTargetingEntities.size());

  }

  @Test
  public void shouldReturnReferencedTargets() throws IllegalAccessException {
    // Given
    NewsTargetingServiceImpl newsTargetingService = new NewsTargetingServiceImpl(metadataService, identityManager);
    org.exoplatform.services.security.Identity identity = new org.exoplatform.services.security.Identity("root");
    List<Metadata> newsTargets = new LinkedList<>();
    Metadata sliderNews = new Metadata();
    sliderNews.setName("sliderNews");
    sliderNews.setCreatedDate(100);
    HashMap<String, String> sliderNewsProperties = new HashMap<>();
    sliderNewsProperties.put("referenced", "true");
    sliderNews.setProperties(sliderNewsProperties);
    sliderNews.setId(1);
    newsTargets.add(sliderNews);

    when(metadataService.getMetadatasByProperty("referenced","true", 100)).thenReturn(newsTargets);
    List<MembershipEntry> memberships = new LinkedList<>();
    MembershipEntry membershipEntry = new MembershipEntry("/platform/web-contributors", "publisher");
    memberships.add(membershipEntry);
    identity.setMemberships(memberships);

    // When
    List<NewsTargetingEntity> newsTargetingEntities = newsTargetingService.getReferencedTargets(identity);

    // Then
    assertNotNull(newsTargetingEntities);
    assertEquals(1, newsTargetingEntities.size());
    assertEquals("sliderNews", newsTargetingEntities.get(0).getName());
  }

  @Test
  public void shouldReturnNewsTargetsByNewsId() throws Exception {
    // Given
    NewsTargetingServiceImpl newsTargetingService = new NewsTargetingServiceImpl(metadataService, identityManager);
    org.exoplatform.services.security.Identity identity = new org.exoplatform.services.security.Identity("root");
    Metadata sliderNews = new Metadata();
    sliderNews.setName("sliderNews");
    sliderNews.setCreatedDate(100);
    HashMap<String, String> sliderNewsProperties = new HashMap<>();
    sliderNewsProperties.put("referenced", "true");
    sliderNews.setProperties(sliderNewsProperties);
    sliderNews.setId(1);

    List<MetadataItem> metadataItems = new LinkedList<>();
    MetadataItem metadataItem = new MetadataItem();
    metadataItem.setCreatedDate(100);
    metadataItem.setCreatorId(1);
    metadataItem.setId(1);
    metadataItem.setObjectId("123456");
    metadataItem.setMetadata(sliderNews);
    metadataItems.add(metadataItem);

    NewsTargetObject newsTargetObject = new NewsTargetObject("news", "123456", null);
    when(metadataService.getMetadataItemsByMetadataTypeAndObject(NewsTargetingService.METADATA_TYPE.getName(), newsTargetObject)).thenReturn(metadataItems);

    // When
    List<String> newsTargets = newsTargetingService.getTargetsByNewsId("123456");

    // Then
    assertNotNull(newsTargets);
    assertEquals(1, newsTargets.size());
    assertEquals("sliderNews", newsTargets.get(0));
  }

  @Test
  public void testSaveNewsTargets() throws Exception {
    // Given
    NewsTargetingServiceImpl newsTargetingService = new NewsTargetingServiceImpl(metadataService, identityManager);
    org.exoplatform.services.security.Identity identity = new org.exoplatform.services.security.Identity("root");
    Metadata sliderNews = new Metadata();
    sliderNews.setName("sliderNews");
    sliderNews.setCreatedDate(100);
    HashMap<String, String> sliderNewsProperties = new HashMap<>();
    sliderNewsProperties.put("referenced", "true");
    sliderNews.setProperties(sliderNewsProperties);
    sliderNews.setId(1);

    MetadataItem metadataItem = new MetadataItem();
    metadataItem.setCreatedDate(100);
    metadataItem.setCreatorId(1);
    metadataItem.setId(1);
    metadataItem.setObjectId("123456");
    metadataItem.setMetadata(sliderNews);

    List<String> targets = new LinkedList<>();
    targets.add("sliderNews");

    News news = new News();
    news.setSpaceId("spaceId");
    news.setTitle("Test news");
    news.setAuthor("user1");
    news.setTargets(targets);
    news.setId("123456");

    NewsTargetObject newsTargetObject = new NewsTargetObject("news", "123456", null);
    MetadataKey metadataKey = new MetadataKey(NewsTargetingService.METADATA_TYPE.getName(), "sliderNews", 0);
    Identity userIdentity = new Identity("1");
    when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root")).thenReturn(userIdentity);
    when(metadataService.createMetadataItem(newsTargetObject, metadataKey, 1)).thenReturn(metadataItem);
    IdentityRegistry identityRegistry = mock(IdentityRegistry.class);
    Authenticator authenticator = mock(Authenticator.class);
    PowerMockito.mockStatic(ExoContainerContext.class);
    when(ExoContainerContext.getService(IdentityRegistry.class)).thenReturn(identityRegistry);
    when(ExoContainerContext.getService(Authenticator.class)).thenReturn(authenticator);
    when(authenticator.createIdentity("root")).thenReturn(identity);
    List<MembershipEntry> memberships = new LinkedList<>();
    MembershipEntry membershipEntry = new MembershipEntry("/platform/web-contributors", "publisher");
    memberships.add(membershipEntry);
    identity.setMemberships(memberships);
    Map<String, String> properties = new LinkedHashMap<>();
    properties.put(PublicationDefaultStates.STAGED, String.valueOf(false));

    // When
    newsTargetingService.saveNewsTarget(news.getId(), false, news.getTargets(), "root");

    // Then
    verify(identityManager, times(1)).getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    verify(metadataService, times(1)).createMetadataItem(newsTargetObject, metadataKey, properties, 1);

  }

  @Test
  public void testGetNewsTargetItemsByTargetName() throws Exception {
    // Given
    NewsTargetingServiceImpl newsTargetingService = new NewsTargetingServiceImpl(metadataService, identityManager);

    Metadata sliderNews = new Metadata();
    sliderNews.setName("newsTargets");
    sliderNews.setCreatedDate(100);
    HashMap<String, String> sliderNewsProperties = new HashMap<>();
    sliderNewsProperties.put("referenced", "true");
    sliderNews.setProperties(sliderNewsProperties);
    sliderNews.setId(1);

    MetadataItem metadataItem = new MetadataItem();
    metadataItem.setCreatedDate(100);
    metadataItem.setCreatorId(1);
    metadataItem.setId(1);
    metadataItem.setObjectId("123456");
    metadataItem.setMetadata(sliderNews);
    List<MetadataItem> metadataItems = new LinkedList<>();
    metadataItems.add(metadataItem);
    when(metadataService.getMetadataItemsByMetadataNameAndTypeAndObjectAndMetadataItemProperty("newsTargets", NewsTargetingService.METADATA_TYPE.getName(),"news", PublicationDefaultStates.STAGED, String.valueOf(false),0,10)).thenReturn(metadataItems);

    // When
    List<MetadataItem> newsTargetsItems = newsTargetingService.getNewsTargetItemsByTargetName("newsTargets",false, 0, 10);

    // Then
    assertNotNull(newsTargetsItems);
    assertEquals(1, newsTargetsItems.size());
  }

  @Test
  @PrepareForTest({ ExoContainerContext.class })
  public void testDeleteTargetByName() throws IllegalAccessException {
    // Given
    NewsTargetingServiceImpl newsTargetingService = new NewsTargetingServiceImpl(metadataService, identityManager);
    String username = "user";
    Identity userIdentity = new Identity();
    userIdentity.setRemoteId(username);
    when(identityManager.getOrCreateUserIdentity(username)).thenReturn(userIdentity);

    IdentityRegistry identityRegistry = mock(IdentityRegistry.class);
    PowerMockito.mockStatic(ExoContainerContext.class);
    when(ExoContainerContext.getService(IdentityRegistry.class)).thenReturn(identityRegistry);
    org.exoplatform.services.security.Identity identity = mock(org.exoplatform.services.security.Identity.class);
    when(identityRegistry.getIdentity(username)).thenReturn(identity);
    when(identity.isMemberOf("/platform/web-contributors", "manager")).thenReturn(true);

    List<Metadata> newsTargets = new LinkedList<>();
    Metadata sliderNews = new Metadata();
    sliderNews.setName("sliderNews");
    sliderNews.setCreatedDate(100);
    HashMap<String, String> sliderNewsProperties = new HashMap<>();
    sliderNewsProperties.put("label", "slider news");
    sliderNews.setProperties(sliderNewsProperties);
    sliderNews.setId(1);
    newsTargets.add(sliderNews);

    List<NewsTargetingEntity> targets = new LinkedList<>();
    NewsTargetingEntity newsTargetingEntity = new NewsTargetingEntity();
    newsTargetingEntity.setName("test1");
    targets.add(newsTargetingEntity);

    when(newsTargetingService.getTargets()).thenReturn(targets);
    when(metadataService.getMetadataByKey(any())).thenReturn(sliderNews);
    newsTargetingService.deleteTargetByName(targets.get(0).getName(), identity);

    // When
    verify(metadataService, atLeastOnce()).deleteMetadataById(newsTargets.get(0).getId());
  }

  @Test
  public void testCreateTarget() throws IllegalAccessException {
    // Given
    NewsTargetingServiceImpl newsTargetingService = new NewsTargetingServiceImpl(metadataService, identityManager);
    org.exoplatform.services.security.Identity currentIdentity = new org.exoplatform.services.security.Identity("root");
    MembershipEntry membershipentry = new MembershipEntry("/platform/web-contributors", "manager");
    List<MembershipEntry> memberships = new ArrayList<MembershipEntry>();
    memberships.add(membershipentry);
    currentIdentity.setMemberships(memberships);
    Identity userIdentity = new Identity("organization", "root");
    userIdentity.setId("1");
    when(identityManager.getOrCreateIdentity(any(), any())).thenReturn(userIdentity);

    List<Metadata> newsTargets = new LinkedList<>();
    Metadata sliderNews = new Metadata();
    MetadataType metadataType = new MetadataType(4, "newsTarget");
    sliderNews.setType(metadataType);
    sliderNews.setName("sliderNews");
    sliderNews.setCreatorId(1);
    HashMap<String, String> sliderNewsProperties = new HashMap<>();
    sliderNewsProperties.put("label", "slider news");
    sliderNews.setProperties(sliderNewsProperties);
    sliderNews.setId(0);
    newsTargets.add(sliderNews);

    NewsTargetingEntity newsTargetingEntity = new NewsTargetingEntity();
    newsTargetingEntity.setName(sliderNews.getName());
    newsTargetingEntity.setProperties(sliderNews.getProperties());
    when(metadataService.createMetadata(sliderNews, 1)).thenReturn(sliderNews);

    Metadata createdMetadata = newsTargetingService.createNewsTarget(newsTargetingEntity, currentIdentity);

    // Then
    assertNotNull(createdMetadata);
    assertEquals(sliderNews.getId(), createdMetadata.getId());
    assertEquals(sliderNews.getName(), createdMetadata.getName());

    // use case when adding a target with the same name
    when(metadataService.getMetadataByKey(any())).thenReturn(sliderNews);
    try {
      newsTargetingService.createNewsTarget(newsTargetingEntity, currentIdentity);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }

    // use case when adding a target with user that is not a manager
    String username1 = "John";
    String id1 = "2";
    Identity userIdentity1 = new Identity();
    userIdentity1.setId(id1);
    userIdentity1.setRemoteId(username1);
    when(identityManager.getIdentity(id1)).thenReturn(userIdentity1);

    IdentityRegistry identityRegistry1 = mock(IdentityRegistry.class);
    PowerMockito.mockStatic(ExoContainerContext.class);
    when(ExoContainerContext.getService(IdentityRegistry.class)).thenReturn(identityRegistry1);
    org.exoplatform.services.security.Identity identity1 = mock(org.exoplatform.services.security.Identity.class);
    when(identityRegistry1.getIdentity(username1)).thenReturn(identity1);
    when(identity1.isMemberOf("/platform/web-contributors", "publisher")).thenReturn(true);
    try {
      newsTargetingService.createNewsTarget(newsTargetingEntity, identity1);
      fail();
    } catch (IllegalAccessException e) {
      // Expected
    }
  }

  @Test
  public void testUpdateTarget() throws IllegalAccessException {
    // Given
    NewsTargetingServiceImpl newsTargetingService = new NewsTargetingServiceImpl(metadataService, identityManager);
    org.exoplatform.services.security.Identity currentIdentity = new org.exoplatform.services.security.Identity("root");
    MembershipEntry membershipentry = new MembershipEntry("/platform/web-contributors", "manager");
    List<MembershipEntry> memberships = new ArrayList<MembershipEntry>();
    memberships.add(membershipentry);
    currentIdentity.setMemberships(memberships);
    Identity userIdentity = new Identity("organization", "root");
    userIdentity.setId("1");
    when(identityManager.getOrCreateIdentity(any(), any())).thenReturn(userIdentity);

    List<Metadata> newsTargets = new LinkedList<>();
    Metadata sliderNews = new Metadata();
    MetadataType metadataType = new MetadataType(4, "newsTarget");
    sliderNews.setType(metadataType);
    sliderNews.setName("sliderNews");
    sliderNews.setCreatorId(1);
    HashMap<String, String> sliderNewsProperties = new HashMap<>();
    sliderNewsProperties.put("label", "slider news");
    sliderNewsProperties.put("description", "description slider news");
    sliderNews.setProperties(sliderNewsProperties);
    sliderNews.setId(0);
    newsTargets.add(sliderNews);

    NewsTargetingEntity newsTargetingEntity = new NewsTargetingEntity();
    newsTargetingEntity.setName(sliderNews.getName());
    newsTargetingEntity.setProperties(sliderNews.getProperties());
    when(metadataService.createMetadata(sliderNews, 1)).thenReturn(sliderNews);

    Metadata createdMetadata = newsTargetingService.createNewsTarget(newsTargetingEntity, currentIdentity);

    String originalTargetName = "sliderNews";
    NewsTargetingEntity newsTargetingEntityUpdated = new NewsTargetingEntity();
    newsTargetingEntityUpdated.setName("sliderNews update");
    newsTargetingEntityUpdated.setProperties(sliderNews.getProperties());
    MetadataKey targetMetadataKey = new MetadataKey(NewsTargetingService.METADATA_TYPE.getName(), originalTargetName, 0);
    when(metadataService.updateMetadata(createdMetadata, 1)).thenReturn(sliderNews);
    when(metadataService.getMetadataByKey(targetMetadataKey)).thenReturn(createdMetadata);

    Metadata updatedMetadata = newsTargetingService.updateNewsTargets(originalTargetName, newsTargetingEntityUpdated, currentIdentity);

    // Then
    assertNotNull(updatedMetadata);
    assertEquals(sliderNews.getId(), updatedMetadata.getId());
    assertEquals(sliderNews.getName(), updatedMetadata.getName());

    // use case when updating a target with the same name and same description
    when(metadataService.updateMetadata(createdMetadata, 1)).thenReturn(sliderNews);
    when(metadataService.getMetadataByKey(any())).thenReturn(sliderNews);
    try {
      newsTargetingService.updateNewsTargets(originalTargetName, newsTargetingEntity, currentIdentity);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }

    when(metadataService.updateMetadata(createdMetadata, 1)).thenReturn(sliderNews);
    when(metadataService.getMetadataByKey(any())).thenReturn(null);
    try {
      newsTargetingService.updateNewsTargets(originalTargetName, newsTargetingEntity, currentIdentity);
      fail();
    } catch (IllegalStateException e) {
      // Expected
    }

  }

}
