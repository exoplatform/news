/*
  Copyright (C) 2023 eXo Platform SAS.

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
export function initExtensions() {
  extensionRegistry.registerExtension('AnalyticsTable', 'CellValue', {
    type: 'news',
    options: {
      // Rank of executing 'match' method
      rank: 60,
      // Used Vue component to display cell value
      vueComponent: Vue.options.components['analytics-table-cell-content-value'],
      // Method complete signature : match: (fieldName, aggregationType, fieldDataType, item) => { ... }
      match: (fieldName, aggregationType) => fieldName === 'contentId.keyword' && aggregationType === 'TERMS',
    },
  });

  extensionRegistry.registerExtension('AnalyticsSamples', 'SampleItem', {
    type: 'news',
    options: {
      // Rank of executing 'match' method
      rank: 30,
      // Used Vue component to display cell value
      vueComponent: Vue.options.components['analytics-sample-item-content'],
      match: fieldName => fieldName === 'contentId',
    },
  });
}
