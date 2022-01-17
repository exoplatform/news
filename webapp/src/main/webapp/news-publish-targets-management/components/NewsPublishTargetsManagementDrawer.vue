<template>
  <exo-drawer
    id="newsPublishTargetsDrawer"
    ref="newsPublishTargetsDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    right>
    <template slot="title">
      {{ $t('news.publishTargets.management.addTarget') }}
    </template>
    <template slot="content">
      <v-form
        ref="activityShareFrom"
        class="flex mx-4"
        flat>
        <div class="d-flex flex-column flex-grow-1">
          <div class="d-flex flex-row mt-6 mb-1">
            <label class="text-subtitle-1 font-weight-bold">
              {{ $t('news.publishTargets.managementDrawer.name') }}*
            </label>
          </div>
          <div class="d-flex flex-row">
            <input
              id="nameTarget"
              ref="nameTarget"
              v-model="nameTarget"
              :placeholder="$t('news.publishTargets.placeholderDrawer.nameTarget')"
              type="text"
              name="title"
              class="ignore-vuetify-classes nameTarget"
              required
              autofocus>
          </div>
          <div class="d-flex flex-row mt-6">
            <label class="text-subtitle-1 font-weight-bold">
              {{ $t('news.publishTargets.managementDrawer.description') }}
            </label>
          </div>
          <div class="d-flex flex-row">
            <extended-textarea
              id="targetDescription"
              ref="targetDescription"
              v-model="targetDescription"
              :placeholder="$t('news.publishTargets.placeholderDrawer.descriptionTarget')"
              :max-length="targetDescriptionTextLength"
              class="targetDescription pt-0 " />
          </div>
        </div>
      </v-form>
    </template>
    <template slot="footer">
      <div class="d-flex justify-end">
        <v-btn
          class="btn ms-2"
          @click="closeDrawer">
          {{ $t('news.publishTargets.management.btn.cancel') }}
        </v-btn>
        <v-btn
          :disabled="true"
          class="btn btn-primary ms-2">
          {{ $t('news.publishTargets.management.btn.confirm') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  data: () => ({
    drawer: false,
    targetDescriptionTextLength: 100,
    targetDescription: '',
    nameTarget: '',
  }),
  methods: {
    open() {
      this.$refs.newsPublishTargetsDrawer.open();
    },
    closeDrawer() {
      this.$refs.newsPublishTargetsDrawer.close();
    }
  }
};
</script>