<template>
  <div @keydown.esc="closeDraftNewsDrawer()">
    <button id="newsDraftButton"
            :disabled="newsDrafts.length == 0"
            class="btn"
            @click="openDraftNewsDrawer()">
      {{ $t("news.composer.draft", {0: newsDrafts.length}) }}
    </button>
    <div v-show="newsDrafts.length != 0" :class="{open}" class="drawer">
      <div class="header">
        <span>{{ $t('news.drafts.title', {0: newsDrafts.length}) }}</span>
        <a class="closebtn" href="javascript:void(0)" @click="closeDraftNewsDrawer()">×</a>
      </div>
      <div class="content">
        <ul id="newsDraftsList">
          <li v-for="(draft, index) in newsDrafts" :key="draft" class="newsDraftEntry">
            <div class="newsDetails">
              <div class="draftDescription">
                <p v-if="draft.title === ''" class="draftTitle" @click="selectedDraft(draft.id)"><b>{{ $t('news.drafts.draft.title.none') }}</b></p>
                <p v-else class="draftTitle" @click="selectedDraft(draft.id)"><b> {{ draft.title }} </b></p>
                <p class="draftModifiedTime">Last modified {{ new Date(draft.updateDate.time).toLocaleDateString() }} at {{ new Date(draft.updateDate.time).toLocaleTimeString() }}</p>
              </div>
              <img v-if="draft.illustration" :src="draft.illustrationURL" class="draftIllustration">
            </div>
            <div :class="'draft'+index.toString()" class="draftButtons">
              <div class="draftButton" @click="selectedDraft(draft.id)"><i class="uiIconEdit"></i>
                <a>{{ $t('news.drafts.btn.update') }}</a>
              </div>
              <div class="draftButton delete" @click="deleteDraft(index)">
                <i class="uiIconDelete"></i> <a>{{ $t('news.drafts.btn.delete') }}</a>
              </div>
            </div>

            <div :class="'draft'+index.toString()" class="deleteDraftConfirmation">
              <p>{{ $t("news.drafts.delete.confirmation.message") }}</p>
              <a class="confirmationButtons cancel" @click="cancelDelete(index)">{{ $t('news.drafts.delete.confirmation.btn.cancel') }}</a>
              <a class="confirmationButtons delete" @click="confirmDelete(draft.id, index)">{{ $t('news.drafts.delete.confirmation.btn.delete') }}</a>
            </div>
            <hr>
          </li>
        </ul>
      </div>
    </div>
    <div v-show="showDraftNews" class="drawer-backdrop" @click="closeDraftNewsDrawer()"></div>
  </div>
</template>

<script>
import * as newsServices from '../../services/newsServices';

export default {
  props: {
    spaceId: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      showDraftNews: false,
      confirmDeleteDraft: false,
      newsDrafts: [],
      draft: {
        illustration: []
      },
      open: false
    };
  },
  created() {
    this.$parent.$on('draftCreated', this.loadNewsDrafts);
    this.$parent.$on('draftUpdated', this.loadNewsDrafts);
    this.$parent.$on('draftDeleted', this.loadNewsDrafts);
    this.loadNewsDrafts();
  },
  methods: {
    loadNewsDrafts: function() {
      newsServices.getNewsDrafts(this.spaceId).then(
        (data) => {
          this.newsDrafts = data.sort((draft1, draft2) => draft2.updateDate.time - draft1.updateDate.time);
          data.map((draft) => {
            if(draft.illustration){
              draft.illustrationURL = `/portal/rest/v1/news/${draft.id}/illustration`;
            }
          });
        }
      );
    },
    openDraftNewsDrawer: function() {
      this.open = true;
      $('.drawer').css({ 'right': '0px', 'left': '' }).animate({
        'right' : '0px'
      });
      this.showDraftNews = true;
      $('body').css({overflow:'hidden'});
    },
    closeDraftNewsDrawer: function () {
      this.open = false;
      $('.drawer').css({ 'right': '', 'left': '' }).animate({
        'right' : '-33%'
      });
      this.showDraftNews = false;
    },
    selectedDraft: function (draftId) {
      this.$emit('draftSelected', draftId);
      this.closeDraftNewsDrawer();
    },
    deleteDraft: function (draftIndex) {
      const draftButtons = document.querySelector(`.draftButtons.draft${draftIndex}`);
      const draftDeleteConfirm = document.querySelector(`.deleteDraftConfirmation.draft${draftIndex}`);
      draftButtons.style.display = 'none';
      draftDeleteConfirm.style.display = 'block';
    },
    cancelDelete: function (draftIndex) {
      const draftButtons = document.querySelector(`.draftButtons.draft${draftIndex}`);
      const draftDeleteConfirm = document.querySelector(`.deleteDraftConfirmation.draft${draftIndex}`);
      draftDeleteConfirm.style.display = 'none';
      draftButtons.style.display = 'block';
    },
    confirmDelete: function (draftId, index) {
      newsServices.deleteDraft(draftId);
      this.newsDrafts.splice(index, 1);
      if (this.$router.history.current.fullPath.split('/')['2']  === draftId) {
        this.$router.push({name: 'NewsComposer', params: {action: 'post'}});
      }
    }
  }
};
</script>
