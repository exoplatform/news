<!--
Copyright (C) 2021 eXo Platform SAS.

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
-->
<template>
  <div class="card" id="card-$uuid">
    <a
      class="articleLink"
      target="_self"
      href="$itemLink">
      <img class="article-img" src="https://picsum.photos/500">
      <div class="author-date-container">
        <img class="author-photo" src="https://www.rd.com/wp-content/uploads/2017/09/01-shutterstock_476340928-Irina-Bg.jpg">
        <div class="date">Apr 17</div>
      </div>
    </a>
    <a
      class="articleLink"
      target="_self"
      href="$itemLink">
      <div class="title-container">
        <div class="article-title">All you need to know about the ACME group acquisition you need to know about the HG</div>
        <div class="article-counters">
          <div class="reactions">
            <i class="likes-icon">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                version="1.1"
                id="Capa_1"
                x="0px"
                y="0px"
                viewBox="0 0 489.543 489.543"
                style="enable-background:new 0 0 489.543 489.543;"
                xml:space="preserve">
                <path d="M270.024,0c-22.6,0-15,48.3-15,48.3s-48.3,133.2-94.5,168.7c-9.9,10.4-16.1,21.9-20,31.3l0,0l0,0    c-0.9,2.3-1.7,4.5-2.4,6.5c-3.1,6.3-9.7,16-23.8,24.5l46.2,200.9c0,0,71.5,9.3,143.2,7.8c28.7,2.3,59.1,2.5,83.3-2.7    c82.2-17.5,61.6-74.8,61.6-74.8c44.3-33.3,19.1-74.9,19.1-74.9c39.4-41.1,0.7-75.6,0.7-75.6s21.3-33.2-6.2-58.3    c-34.3-31.4-127.4-10.5-127.4-10.5l0,0c-6.5,1.1-13.4,2.5-20.8,4.3c0,0-32.2,15,0-82.7C346.324,15.1,292.624,0,270.024,0z"></path>
                <path d="M127.324,465.7l-35-166.3c-2-9.5-11.6-17.3-21.3-17.3h-66.8l-0.1,200.8h109.1C123.024,483,129.324,475.2,127.324,465.7z"></path>
              </svg>
            </i>
            <div class="likes-count">25</div>
            <i class="comments-icon">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                version="1.1"
                id="Capa_1"
                x="0px"
                y="0px"
                viewBox="0 0 60 60"
                style="enable-background:new 0 0 60 60;"
                xml:space="preserve">
                <path d="M55.232,43.104C58.354,38.746,60,33.705,60,28.5c0-14.888-13.458-27-30-27S0,13.612,0,28.5s13.458,27,30,27  c4.262,0,8.378-0.79,12.244-2.348c6.805,3.927,16.212,5.282,16.618,5.338c0.046,0.007,0.093,0.01,0.139,0.01  c0.375,0,0.725-0.211,0.895-0.554c0.192-0.385,0.116-0.849-0.188-1.153C57.407,54.493,55.823,49.64,55.232,43.104z"></path>
              </svg>
            </i>
            <div class="comments-count">12</div>
            <div class="views">
              <i class="views-icon">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  xmlns:xlink="http://www.w3.org/1999/xlink"
                  version="1.1"
                  id="Capa_1"
                  x="0px"
                  y="0px"
                  viewBox="0 0 488.85 488.85"
                  style="enable-background:new 0 0 488.85 488.85;"
                  xml:space="preserve">
                  <path d="M244.425,98.725c-93.4,0-178.1,51.1-240.6,134.1c-5.1,6.8-5.1,16.3,0,23.1c62.5,83.1,147.2,134.2,240.6,134.2   s178.1-51.1,240.6-134.1c5.1-6.8,5.1-16.3,0-23.1C422.525,149.825,337.825,98.725,244.425,98.725z M251.125,347.025   c-62,3.9-113.2-47.2-109.3-109.3c3.2-51.2,44.7-92.7,95.9-95.9c62-3.9,113.2,47.2,109.3,109.3   C343.725,302.225,302.225,343.725,251.125,347.025z M248.025,299.625c-33.4,2.1-61-25.4-58.8-58.8c1.7-27.6,24.1-49.9,51.7-51.7   c33.4-2.1,61,25.4,58.8,58.8C297.925,275.625,275.525,297.925,248.025,299.625z"></path>
                </svg>
              </i>
              <div class="views-count">333</div>
            </div>
          </div>
        </div>
      </div>
    </a>

          
    <div v-if="seeAllArticles">
      <a
        class="see-all-link"
        target="_self"
        href="/portal/dw/news">
        <div class="card" id="see-all">
          <div class="see-all-icon">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              xmlns:xlink="http://www.w3.org/1999/xlink"
              version="1.1"
              id="Layer_1"
              x="0px"
              y="0px"
              viewBox="0 0 330 330"
              style="enable-background:new 0 0 330 330;"
              xml:space="preserve">
              <path d="M165,0C74.019,0,0,74.019,0,165s74.019,165,165,165s165-74.019,165-165S255.981,0,165,0z M85,190  c-13.785,0-25-11.215-25-25s11.215-25,25-25s25,11.215,25,25S98.785,190,85,190z M165,190c-13.785,0-25-11.215-25-25  s11.215-25,25-25s25,11.215,25,25S178.785,190,165,190z M245,190c-13.785,0-25-11.215-25-25s11.215-25,25-25  c13.785,0,25,11.215,25,25S258.785,190,245,190z"></path>
            </svg>
          </div>
          <div class="see-all-text">See all articles</div>
        </div>
      </a>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    seeAllArticles: {
      type: Boolean,
      default: false,
    },
  },
};
</script>
