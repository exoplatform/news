<template>
  <v-app>
    <exo-drawer
      ref="publishNewsDrawer"
      body-classes="hide-scroll decrease-z-index-more"
      right>
      <template slot="title">
        {{ $t('news.composer.postArticle') }}
      </template>
      <template slot="content">
        <v-radio-group v-model="postArticleMode" class="ml-2">
          <v-radio
            :label="$t('news.composer.postImmediately')"
            value="immediate" />
          <v-radio
            :label="$t('news.composer.postLater')"
            value="later" />
        </v-radio-group>
        <div v-if="postArticleMode==='later'" class="ml-4">
          <div class="grey--text my-4">{{ $t('news.composer.chooseDatePublish') }}</div>
          <div class="d-flex flex-row flex-grow-1">
            <date-picker
              v-model="datePublished"
              class="flex-grow-1 my-auto" />
            <div class="d-flex flex-row flex-grow-0">
              <slot name="datePublishedDateTime"></slot>
              <time-picker v-model="datePublishedTime" class="me-4" />
            </div>
          </div>
        </div>
      </template>
      <template slot="footer">
        <div class="d-flex justify-end">
          <v-btn
            :disabled="disabled"
            class="btn btn-primary ms-2"
            @click="selectPostMode">
            {{ $t('news.composer.post') }}
          </v-btn>
        </div>
      </template>
    </exo-drawer>
  </v-app>
</template>

<script>
export default {
  data: () => ({
    disabled: true,
    showDatePublishing: false,
    postArticleMode: 'immediate',
    datePublished: null,
    datePublishedTime: null,
  }),
  methods: {
    open() {
      if (this.$refs.publishNewsDrawer) {
        this.disabled = false;
        this.$refs.publishNewsDrawer.open();
      }
    },
    selectPostMode() {
      this.$emit('post-article', this.postArticleMode);
    },
  }
};
</script>