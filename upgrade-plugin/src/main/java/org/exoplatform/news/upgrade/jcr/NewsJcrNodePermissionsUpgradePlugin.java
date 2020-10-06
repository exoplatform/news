/*
 * Copyright (C) 2003-2020 eXo Platform SAS.
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
package org.exoplatform.news.upgrade.jcr;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS Author : Ayoub Zayati Sept 14, 2020 This
 * plugin will be executed in order to remove administrators permission from
 * each space news folder
 */
public class NewsJcrNodePermissionsUpgradePlugin extends UpgradeProductPlugin {

  private static final Log       log                     = ExoLogger.getLogger(NewsJcrNodePermissionsUpgradePlugin.class.getName());

  private NodeHierarchyCreator   nodeHierarchyCreator;

  private RepositoryService      repositoryService;

  private SessionProviderService sessionProviderService;

  private int                    newsJcrNodesUpdatedCount;

  public static final String     NEWS_NODES_FOLDER       = "News";

  public static final String     ADMINISTRATORS_IDENTITY = "*:/platform/administrators";

  public NewsJcrNodePermissionsUpgradePlugin(InitParams initParams,
                                             NodeHierarchyCreator nodeHierarchyCreator,
                                             RepositoryService repositoryService,
                                             SessionProviderService sessionProviderService) {
    super(initParams);
    this.nodeHierarchyCreator = nodeHierarchyCreator;
    this.repositoryService = repositoryService;
    this.sessionProviderService = sessionProviderService;
  }

  @Override
  public void processUpgrade(String oldVersion, String newVersion) {
    long startupTime = System.currentTimeMillis();
    log.info("Start upgrade of news jcr nodes");

    SessionProvider sessionProvider = null;
    try {
      sessionProvider = sessionProviderService.getSystemSessionProvider(null);
      Session session = sessionProvider.getSession(
                                                   repositoryService.getCurrentRepository()
                                                                    .getConfiguration()
                                                                    .getDefaultWorkspaceName(),
                                                   repositoryService.getCurrentRepository());

      String spacesNodePath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_GROUPS_PATH) + "/spaces";
      Node spacesRootNode = (Node) session.getItem(spacesNodePath);
      NodeIterator iter = spacesRootNode.getNodes();
      while (iter.hasNext()) {
        Node spaceNode = iter.nextNode();
        if (spaceNode.hasNode(NEWS_NODES_FOLDER)) {
          Node spaceNewsRootNode = spaceNode.getNode(NEWS_NODES_FOLDER);
          ((ExtendedNode) spaceNewsRootNode).removePermission(ADMINISTRATORS_IDENTITY);
          spaceNewsRootNode.save();
          newsJcrNodesUpdatedCount ++;
        }
      }
      log.info("End upgrade of '{}' news jcr nodes. It took {} ms",
               newsJcrNodesUpdatedCount,
               (System.currentTimeMillis() - startupTime));
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("An unexpected error occurs when migrating news jcr node permissions:", e);
      }
    } finally {
      if (sessionProvider != null) {
        sessionProvider.close();
      }
    }
  }
  
  /**
   * @return the newsJcrNodesUpdatedCount
   */
  public int getNewsJcrNodesUpdatedCount() {
    return newsJcrNodesUpdatedCount;
  }

}
