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
        </v-radio-group>
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
    postArticleMode: 'immediate',
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