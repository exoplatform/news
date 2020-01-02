<template>
  <div class="newsEditComponent">
    <a id="newsEditButton" :data-original-title="$t('news.edit.edit')" :href="editLink"
       :target="openTarget"
       class="btn"
       rel="tooltip"
       data-placement="bottom">
      <i class="uiIconEdit"></i>
    </a>
  </div>
</template>

<script>

export default {
  props: {
    newsId: {
      type: String,
      required: true
    },
    activityId: {
      type: String,
      required: false,
      default: ''
    },
    openTarget: {
      type: String,
      required: false,
      default: '_self'
    }
  },
  computed: {
    editLink: function() {
      return `${eXo.env.portal.context}/${eXo.env.portal.portalName}/news/editor?newsId=${this.newsId}&activityId=${this.activityId}`;
    }
  }
};
</script>
