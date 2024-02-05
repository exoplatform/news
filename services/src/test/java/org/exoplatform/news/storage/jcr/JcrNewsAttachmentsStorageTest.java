package org.exoplatform.news.storage.jcr;

import static org.mockito.Mockito.*;

import org.exoplatform.news.model.News;
import org.exoplatform.news.model.NewsAttachment;
import org.exoplatform.news.storage.NewsAttachmentsStorage;
import org.exoplatform.news.storage.jcr.JcrNewsAttachmentsStorage;
import org.exoplatform.services.cms.documents.DocumentService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.distribution.DataDistributionManager;
import org.exoplatform.services.jcr.ext.distribution.DataDistributionMode;
import org.exoplatform.services.jcr.ext.distribution.DataDistributionType;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.impl.core.value.StringValue;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalMatchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;
import javax.jcr.Value;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class JcrNewsAttachmentsStorageTest {

  @Mock
  RepositoryService       repositoryService;

  @Mock
  SessionProviderService  sessionProviderService;

  @Mock
  ManageableRepository repository;

  @Mock
  RepositoryEntry repositoryEntry;

  @Mock
  SessionProvider sessionProvider;

  @Mock
  Session session;

  @Mock
  NodeHierarchyCreator    nodeHierarchyCreator;

  @Mock
  DataDistributionManager dataDistributionManager;

  @Mock
  SpaceService            spaceService;

  @Mock
  UploadService           uploadService;

  @Mock
  DocumentService         documentService;

  @Test
  public void shouldGetAttachmentWhenItExists() throws Exception {
    // Given
    NewsAttachmentsStorage newsAttachmentsService = new JcrNewsAttachmentsStorage(sessionProviderService,
                                                                                   repositoryService,
                                                                                   nodeHierarchyCreator,
                                                                                   dataDistributionManager,
                                                                                   spaceService,
                                                                                   uploadService,
                                                                                   documentService);
    lenient().when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(repositoryService.getCurrentRepository()).thenReturn(repository);
    lenient().when(repository.getConfiguration()).thenReturn(repositoryEntry);
    lenient().when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    lenient().when(sessionProvider.getSession(any(), any())).thenReturn(session);
    Node node = mock(Node.class);
    lenient().when(node.getUUID()).thenReturn("id123");
    lenient().when(node.getName()).thenReturn("name123");
    lenient().when(session.getNodeByUUID(eq("id123"))).thenReturn(node);
    Node resourceNode = mock(Node.class);
    Property mimetypeProperty = mock(Property.class);
    lenient().when(mimetypeProperty.getString()).thenReturn("image/png");
    lenient().when(resourceNode.hasProperty(eq("jcr:mimeType"))).thenReturn(true);
    lenient().when(resourceNode.getProperty(eq("jcr:mimeType"))).thenReturn(mimetypeProperty);
    Property dataProperty = mock(Property.class);
    lenient().when(dataProperty.getStream()).thenReturn(new ByteArrayInputStream("image".getBytes()));
    lenient().when(resourceNode.getProperty(eq("jcr:data"))).thenReturn(dataProperty);
    lenient().when(node.getNode(eq("jcr:content"))).thenReturn(resourceNode);


    // When
    NewsAttachment newsAttachment = newsAttachmentsService.getNewsAttachment("id123");

    // Then
    assertNotNull(newsAttachment);
    assertEquals("id123", newsAttachment.getId());
    assertEquals("name123", newsAttachment.getName());
    assertEquals("image/png", newsAttachment.getMimetype());
    assertEquals("image".getBytes().length, newsAttachment.getSize());
  }

  @Test
  public void shouldNotGetAttachmentWhenItDoesNotExist() throws Exception {
    // Given
    NewsAttachmentsStorage newsAttachmentsService = new JcrNewsAttachmentsStorage(sessionProviderService,
            repositoryService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            uploadService,
            documentService);
    lenient().when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(repositoryService.getCurrentRepository()).thenReturn(repository);
    lenient().when(repository.getConfiguration()).thenReturn(repositoryEntry);
    lenient().when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    lenient().when(sessionProvider.getSession(any(), any())).thenReturn(session);
    lenient().when(session.getNodeByUUID(eq("id123"))).thenReturn(null);


    // When
    NewsAttachment newsAttachment = newsAttachmentsService.getNewsAttachment("id123");

    // Then
    assertNull(newsAttachment);
  }

  @Test
  public void shouldNotGetAttachmentWhenItHasNoResourceNode() throws Exception {
    // Given
    NewsAttachmentsStorage newsAttachmentsService = new JcrNewsAttachmentsStorage(sessionProviderService,
            repositoryService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            uploadService,
            documentService);
    lenient().when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(repositoryService.getCurrentRepository()).thenReturn(repository);
    lenient().when(repository.getConfiguration()).thenReturn(repositoryEntry);
    lenient().when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    lenient().when(sessionProvider.getSession(any(), any())).thenReturn(session);
    Node node = mock(Node.class);
    lenient().when(node.getUUID()).thenReturn("id123");
    lenient().when(node.getName()).thenReturn("name123");
    lenient().when(node.getNode(eq("jcr:content"))).thenReturn(null);
    lenient().when(session.getNodeByUUID(eq("id123"))).thenReturn(node);


    // When
    NewsAttachment newsAttachment = newsAttachmentsService.getNewsAttachment("id123");

    // Then
    assertNotNull(newsAttachment);
    assertEquals("id123", newsAttachment.getId());
    assertEquals("name123", newsAttachment.getName());
    assertEquals(0, newsAttachment.getSize());
    assertEquals("", newsAttachment.getMimetype());
  }

  @Test
  public void shouldGetAttachmentsOfANewsWhenThereAre() throws Exception {
    // Given
    NewsAttachmentsStorage newsAttachmentsService = new JcrNewsAttachmentsStorage(sessionProviderService,
            repositoryService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            uploadService,
            documentService);
    lenient().when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(repositoryService.getCurrentRepository()).thenReturn(repository);
    lenient().when(repository.getConfiguration()).thenReturn(repositoryEntry);
    lenient().when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    lenient().when(sessionProvider.getSession(any(), any())).thenReturn(session);
    Node node = mock(Node.class);
    lenient().when(node.getUUID()).thenReturn("id123");
    lenient().when(node.getName()).thenReturn("name123");
    lenient().when(node.getSession()).thenReturn(session);
    Property attachmentIdsProperty = mock(Property.class);
    lenient().when(attachmentIdsProperty.getValues()).thenReturn(new Value[] { new StringValue("idAttach1"), new StringValue("idAttach2"), new StringValue("idAttach3") });
    lenient().when(node.hasProperty(eq("exo:attachmentsIds"))).thenReturn(true);
    lenient().when(node.getProperty(eq("exo:attachmentsIds"))).thenReturn(attachmentIdsProperty);
    lenient().when(session.getNodeByUUID(eq("id123"))).thenReturn(node);
    Node attachmentNode = mock(Node.class);
    lenient().when(attachmentNode.getPath()).thenReturn("/folder/subFolder/attachNode");
    Node attachmentNode1 = mock(Node.class);
    lenient().when(attachmentNode1.getPath()).thenReturn("/folder/subFolder/attachNode1");
    Node attachmentNode2 = mock(Node.class);
    // quarantined attachment
    lenient().when(attachmentNode2.getPath()).thenReturn("/Quarantine/attachNode2");
    lenient().when(session.getNodeByUUID(eq("idAttach1"))).thenReturn(attachmentNode);
    lenient().when(session.getNodeByUUID(eq("idAttach2"))).thenReturn(attachmentNode1);
    lenient().when(session.getNodeByUUID(eq("idAttach3"))).thenReturn(attachmentNode2);


    // When
    List<NewsAttachment> newsAttachments = newsAttachmentsService.getNewsAttachments(node);

    // Then
    assertNotNull(newsAttachments);
    assertEquals(2, newsAttachments.size());
  }

  @Test
  public void shouldGetNoAttachmentsOfANewsWhenThereAreNot() throws Exception {
    // Given
    NewsAttachmentsStorage newsAttachmentsService = new JcrNewsAttachmentsStorage(sessionProviderService,
            repositoryService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            uploadService,
            documentService);
    lenient().when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(repositoryService.getCurrentRepository()).thenReturn(repository);
    lenient().when(repository.getConfiguration()).thenReturn(repositoryEntry);
    lenient().when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    lenient().when(sessionProvider.getSession(any(), any())).thenReturn(session);
    Node node = mock(Node.class);
    lenient().when(node.getUUID()).thenReturn("id123");
    lenient().when(node.getName()).thenReturn("name123");
    lenient().when(node.getSession()).thenReturn(session);
    Property attachmentIdsProperty = mock(Property.class);
    lenient().when(attachmentIdsProperty.getValues()).thenReturn(new Value[] {});
    lenient().when(node.hasProperty(eq("exo:attachmentsIds"))).thenReturn(true);
    lenient().when(node.getProperty(eq("exo:attachmentsIds"))).thenReturn(attachmentIdsProperty);
    lenient().when(session.getNodeByUUID(eq("id123"))).thenReturn(node);


    // When
    List<NewsAttachment> newsAttachments = newsAttachmentsService.getNewsAttachments(node);

    // Then
    assertNotNull(newsAttachments);
    assertEquals(0, newsAttachments.size());
  }

  @Test
  public void shouldGetAttachmentStreamWhenAttachmentExists() throws Exception {
    // Given
    NewsAttachmentsStorage newsAttachmentsService = new JcrNewsAttachmentsStorage(sessionProviderService,
            repositoryService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            uploadService,
            documentService);
    lenient().when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(repositoryService.getCurrentRepository()).thenReturn(repository);
    lenient().when(repository.getConfiguration()).thenReturn(repositoryEntry);
    lenient().when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    lenient().when(sessionProvider.getSession(any(), any())).thenReturn(session);
    Node node = mock(Node.class);
    lenient().when(node.getUUID()).thenReturn("id123");
    lenient().when(node.getName()).thenReturn("name123");
    lenient().when(session.getNodeByUUID(eq("id123"))).thenReturn(node);
    Node resourceNode = mock(Node.class);
    Property dataProperty = mock(Property.class);
    lenient().when(dataProperty.getStream()).thenReturn(new ByteArrayInputStream("image".getBytes()));
    lenient().when(resourceNode.getProperty(eq("jcr:data"))).thenReturn(dataProperty);
    lenient().when(node.getNode(eq("jcr:content"))).thenReturn(resourceNode);


    // When
    InputStream stream = newsAttachmentsService.getNewsAttachmentStream("id123");

    // Then
    assertNotNull(stream);
    assertEquals("image".getBytes().length, stream.available());
  }

  @Test
  public void shouldNotGetAttachmentStreamWhenAttachmentDoesNotExist() throws Exception {
    // Given
    NewsAttachmentsStorage newsAttachmentsService = new JcrNewsAttachmentsStorage(sessionProviderService,
            repositoryService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            uploadService,
            documentService);
    lenient().when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(repositoryService.getCurrentRepository()).thenReturn(repository);
    lenient().when(repository.getConfiguration()).thenReturn(repositoryEntry);
    lenient().when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    lenient().when(sessionProvider.getSession(any(), any())).thenReturn(session);
    lenient().when(session.getNodeByUUID(eq("id123"))).thenReturn(null);


    // When
    InputStream stream = newsAttachmentsService.getNewsAttachmentStream("id123");

    // Then
    assertNull(stream);
  }

  @Test
  public void shouldAddAttachmentFromUploadResource() throws Exception {
    // Given
    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
    lenient().when(dataDistributionManager.getDataDistributionType(eq(DataDistributionMode.NONE))).thenReturn(dataDistributionType);
    NewsAttachmentsStorage newsAttachmentsService = new JcrNewsAttachmentsStorage(sessionProviderService,
            repositoryService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            uploadService,
            documentService);
    lenient().when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(repositoryService.getCurrentRepository()).thenReturn(repository);
    lenient().when(repository.getConfiguration()).thenReturn(repositoryEntry);
    lenient().when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    lenient().when(sessionProvider.getSession(any(), any())).thenReturn(session);
    Node node = mock(Node.class);
    lenient().when(node.getUUID()).thenReturn("id123");
    lenient().when(node.getName()).thenReturn("name123");
    Property spacesIdProperty = mock(Property.class);
    lenient().when(spacesIdProperty.getString()).thenReturn("1");
    lenient().when(node.hasProperty(eq("exo:spaceId"))).thenReturn(true);
    lenient().when(node.getProperty(eq("exo:spaceId"))).thenReturn(spacesIdProperty);
    lenient().when(node.getSession()).thenReturn(session);
    Node spaceDocumentsFolderNode = mock(Node.class);
    lenient().when(session.getItem(anyString())).thenReturn(spaceDocumentsFolderNode);
    lenient().when(spaceDocumentsFolderNode.hasNode(eq(JcrNewsAttachmentsStorage.NEWS_ATTACHMENTS_NODES_FOLDER))).thenReturn(true);
    Node spaceNewsRootNode = mock(Node.class);
    lenient().when(spaceDocumentsFolderNode.getNode(eq(JcrNewsAttachmentsStorage.NEWS_ATTACHMENTS_NODES_FOLDER))).thenReturn(spaceNewsRootNode);
    Node newsAttachmentsFolderNode = mock(Node.class);
    lenient().when(dataDistributionType.getOrCreateDataNode(any(), anyString())).thenReturn(newsAttachmentsFolderNode);
    Node newsAttachmentsNode = mock(Node.class);
    lenient().when(newsAttachmentsNode.getUUID()).thenReturn("attachId1");
    lenient().when(newsAttachmentsFolderNode.addNode(anyString(), anyString())).thenReturn(newsAttachmentsNode);
    Node newsAttachmentsResourceNode = mock(Node.class);
    lenient().when(newsAttachmentsNode.addNode(anyString(), anyString())).thenReturn(newsAttachmentsResourceNode);
    lenient().when(session.getNodeByUUID(eq("id123"))).thenReturn(node);
    lenient().when(spaceService.getSpaceById(eq("1"))).thenReturn(new Space());
    String uploadId = "uploadId1";
    lenient().when(uploadService.getUploadResource(eq(uploadId))).thenReturn(new UploadResource(uploadId, "uploaded-file.png"));


    // When
    String attachmentId = newsAttachmentsService.addAttachmentFromUploadedResource(node, uploadId);

    // Then
    assertEquals("attachId1", attachmentId);
  }

  @Test
  public void shouldAddAttachmentFromExistingResourceToNewsHavingAttachments() throws Exception {
    // Given
    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
    lenient().when(dataDistributionManager.getDataDistributionType(eq(DataDistributionMode.NONE))).thenReturn(dataDistributionType);
    NewsAttachmentsStorage newsAttachmentsService = new JcrNewsAttachmentsStorage(sessionProviderService,
            repositoryService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            uploadService,
            documentService);
    lenient().when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(repositoryService.getCurrentRepository()).thenReturn(repository);
    lenient().when(repository.getConfiguration()).thenReturn(repositoryEntry);
    lenient().when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    lenient().when(sessionProvider.getSession(any(), any())).thenReturn(session);
    Node node = mock(Node.class);
    Value[] oldValues = new Value[]{new StringValue("1"),new StringValue("2"),new StringValue("3")};
    Value[] newValues = new Value[]{new StringValue("1"),new StringValue("2"),new StringValue("3"),new StringValue("4")};
    Property attachmentsIdsProperty = mock(Property.class);
    lenient().when(attachmentsIdsProperty.getValues()).thenReturn(oldValues);
    lenient().when(node.hasProperty(eq("exo:attachmentsIds"))).thenReturn(true);
    lenient().when(node.getProperty(eq("exo:attachmentsIds"))).thenReturn(attachmentsIdsProperty);
    lenient().when(node.getSession()).thenReturn(session);
    String uploadId = "4";

    // When
    newsAttachmentsService.addAttachmentFromExistingResource(node, uploadId);

    // Then
    verify(node, times(1)).setProperty(eq("exo:attachmentsIds"), eq(newValues));
  }

  @Test
  public void shouldAddAttachmentFromExistingResourceToNewsNotHavingAttachments() throws Exception {
    // Given
    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
    lenient().when(dataDistributionManager.getDataDistributionType(eq(DataDistributionMode.NONE))).thenReturn(dataDistributionType);
    NewsAttachmentsStorage newsAttachmentsService = new JcrNewsAttachmentsStorage(sessionProviderService,
            repositoryService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            uploadService,
            documentService);
    lenient().when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(repositoryService.getCurrentRepository()).thenReturn(repository);
    lenient().when(repository.getConfiguration()).thenReturn(repositoryEntry);
    lenient().when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    lenient().when(sessionProvider.getSession(any(), any())).thenReturn(session);
    Node node = mock(Node.class);
    lenient().when(node.hasProperty(eq("exo:attachmentsIds"))).thenReturn(false);
    Value[] newValues = new Value[]{new StringValue("1")};
    lenient().when(node.getSession()).thenReturn(session);
    String uploadId = "1";

    // When
    newsAttachmentsService.addAttachmentFromExistingResource(node, uploadId);

    // Then
    verify(node, times(1)).setProperty(eq("exo:attachmentsIds"), eq(newValues));
  }

  @Test
  public void shouldMakeAttachmentsPublic() throws Exception {
    // Given
    NewsAttachmentsStorage newsAttachmentsService = new JcrNewsAttachmentsStorage(sessionProviderService,
            repositoryService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            uploadService,
            documentService);
    lenient().when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(repositoryService.getCurrentRepository()).thenReturn(repository);
    lenient().when(repository.getConfiguration()).thenReturn(repositoryEntry);
    lenient().when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    lenient().when(sessionProvider.getSession(any(), any())).thenReturn(session);
    Node node = mock(Node.class);
    lenient().when(node.getUUID()).thenReturn("id123");
    lenient().when(node.getName()).thenReturn("name123");
    lenient().when(node.getSession()).thenReturn(session);
    lenient().when(session.getNodeByUUID(eq("id123"))).thenReturn(node);
    Property attachmentIdsProperty = mock(Property.class);
    lenient().when(attachmentIdsProperty.getValues()).thenReturn(new Value[] { new StringValue("idAttach1"), new StringValue("idAttach2"), new StringValue("idAttach3") });
    lenient().when(node.hasProperty(eq("exo:attachmentsIds"))).thenReturn(true);
    lenient().when(node.getProperty(eq("exo:attachmentsIds"))).thenReturn(attachmentIdsProperty);
    lenient().when(session.getNodeByUUID(eq("id123"))).thenReturn(node);
    ExtendedNode attachmentNode1 = mock(ExtendedNode.class);
    lenient().when(attachmentNode1.canAddMixin(eq("exo:privilegeable"))).thenReturn(true);
    lenient().when(attachmentNode1.getPath()).thenReturn("/folder/subFolder/attachNode1");
    ExtendedNode attachmentNode2 = mock(ExtendedNode.class);
    lenient().when(attachmentNode2.canAddMixin(eq("exo:privilegeable"))).thenReturn(false);
    lenient().when(attachmentNode2.getPath()).thenReturn("/folder/subFolder/attachNode2");
    ExtendedNode attachmentNode3 = mock(ExtendedNode.class);
    lenient().when(attachmentNode3.canAddMixin(eq("exo:privilegeable"))).thenReturn(true);
    lenient().when(attachmentNode3.getPath()).thenReturn("/folder/subFolder/attachNode3");
    lenient().when(session.getNodeByUUID(eq("idAttach1"))).thenReturn(attachmentNode1);
    lenient().when(session.getNodeByUUID(eq("idAttach2"))).thenReturn(attachmentNode2);
    lenient().when(session.getNodeByUUID(eq("idAttach3"))).thenReturn(attachmentNode3);


    // When
    newsAttachmentsService.makeAttachmentsPublic(node);

    // Then
    verify(attachmentNode1, times(1)).setPermission(eq("any"), AdditionalMatchers.aryEq(new String[] { PermissionType.READ }));
    verify(attachmentNode1, times(1)).addMixin(eq("exo:privilegeable"));
    verify(attachmentNode2, times(1)).setPermission(eq("any"), AdditionalMatchers.aryEq(new String[] { PermissionType.READ }));
    verify(attachmentNode2, times(0)).addMixin(eq("exo:privilegeable"));
    verify(attachmentNode3, times(1)).setPermission(eq("any"), AdditionalMatchers.aryEq(new String[] { PermissionType.READ }));
    verify(attachmentNode3, times(1)).addMixin(eq("exo:privilegeable"));
  }

  @Test
  public void shouldUnmakeAttachmentsPublic() throws Exception {
    // Given
    NewsAttachmentsStorage newsAttachmentsService = new JcrNewsAttachmentsStorage(sessionProviderService,
            repositoryService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            uploadService,
            documentService);
    lenient().when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(repositoryService.getCurrentRepository()).thenReturn(repository);
    lenient().when(repository.getConfiguration()).thenReturn(repositoryEntry);
    lenient().when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    lenient().when(sessionProvider.getSession(any(), any())).thenReturn(session);
    Node node = mock(Node.class);
    lenient().when(node.getUUID()).thenReturn("id123");
    lenient().when(node.getName()).thenReturn("name123");
    lenient().when(node.getSession()).thenReturn(session);
    lenient().when(session.getNodeByUUID(eq("id123"))).thenReturn(node);
    Property attachmentIdsProperty = mock(Property.class);
    lenient().when(attachmentIdsProperty.getValues()).thenReturn(new Value[] { new StringValue("idAttach1"), new StringValue("idAttach2"), new StringValue("idAttach3") });
    lenient().when(node.hasProperty(eq("exo:attachmentsIds"))).thenReturn(true);
    lenient().when(node.getProperty(eq("exo:attachmentsIds"))).thenReturn(attachmentIdsProperty);
    lenient().when(session.getNodeByUUID(eq("id123"))).thenReturn(node);
    ExtendedNode attachmentNode1 = mock(ExtendedNode.class);
    lenient().when(attachmentNode1.isNodeType(eq("exo:privilegeable"))).thenReturn(true);
    lenient().when(attachmentNode1.getPath()).thenReturn("/folder/subFolder/attachNode1");
    ExtendedNode attachmentNode2 = mock(ExtendedNode.class);
    lenient().when(attachmentNode2.isNodeType(eq("exo:privilegeable"))).thenReturn(false);
    lenient().when(attachmentNode2.getPath()).thenReturn("/folder/subFolder/attachNode2");
    ExtendedNode attachmentNode3 = mock(ExtendedNode.class);
    lenient().when(attachmentNode3.isNodeType(eq("exo:privilegeable"))).thenReturn(true);
    lenient().when(attachmentNode3.getPath()).thenReturn("/folder/subFolder/attachNode3");
    lenient().when(session.getNodeByUUID(eq("idAttach1"))).thenReturn(attachmentNode1);
    lenient().when(session.getNodeByUUID(eq("idAttach2"))).thenReturn(attachmentNode2);
    lenient().when(session.getNodeByUUID(eq("idAttach3"))).thenReturn(attachmentNode3);


    // When
    newsAttachmentsService.unmakeAttachmentsPublic(node);

    // Then
    verify(attachmentNode1, times(1)).removePermission(eq("any"));
    verify(attachmentNode2, times(0)).removePermission(eq("any"));
    verify(attachmentNode3, times(1)).removePermission(eq("any"));
  }

  @Test
  public void shouldUpdateAttachments() throws Exception {
    // Given
    NewsAttachmentsStorage newsAttachmentsService = new JcrNewsAttachmentsStorage(sessionProviderService,
            repositoryService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            uploadService,
            documentService);
    lenient().when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(repositoryService.getCurrentRepository()).thenReturn(repository);
    lenient().when(repository.getConfiguration()).thenReturn(repositoryEntry);
    lenient().when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    lenient().when(sessionProvider.getSession(any(), any())).thenReturn(session);
    Node node = mock(Node.class);
    lenient().when(node.getUUID()).thenReturn("id123");
    lenient().when(node.getName()).thenReturn("name123");
    lenient().when(node.getSession()).thenReturn(session);
    Property attachmentIdsProperty = mock(Property.class);
    lenient().when(attachmentIdsProperty.getValues()).thenReturn(new Value[] { new StringValue("idAttach1"), new StringValue("idAttach2"), new StringValue("idAttach3") });
    lenient().when(node.hasProperty(eq("exo:attachmentsIds"))).thenReturn(true);
    lenient().when(node.getProperty(eq("exo:attachmentsIds"))).thenReturn(attachmentIdsProperty);
    lenient().when(session.getNodeByUUID(eq("id123"))).thenReturn(node);
    Node attachmentNode = mock(Node.class);
    lenient().when(attachmentNode.getPath()).thenReturn("/folder/subFolder/attachNode");
    lenient().when(session.getNodeByUUID(eq("idAttach1"))).thenReturn(attachmentNode);
    lenient().when(session.getNodeByUUID(eq("idAttach2"))).thenReturn(attachmentNode);
    lenient().when(session.getNodeByUUID(eq("idAttach3"))).thenReturn(attachmentNode);
    News news = mock(News.class);
    List<NewsAttachment> newsAttachments = newsAttachmentsService.getNewsAttachments(node);

    lenient().when(news.getAttachments()).thenReturn(newsAttachments);

    NewsAttachment newsAttachmentFromUpload = mock(NewsAttachment.class);
    newsAttachmentFromUpload.setUploadId("uploadID123");
    newsAttachmentFromUpload.setId(null);
    NewsAttachment newsAttachmentWithID = mock(NewsAttachment.class);
    newsAttachmentWithID.setId("idNewAttach1");
    newsAttachments.add(newsAttachmentFromUpload);
    newsAttachments.add(newsAttachmentWithID);
    news.setAttachments(newsAttachments);

    // When

    newsAttachments = newsAttachmentsService.updateNewsAttachments(news, node);

    // Then
    assertNotNull(newsAttachments);
    assertEquals(5, newsAttachments.size());

  }

}
