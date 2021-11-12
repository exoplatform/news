<template>
  <v-flex class="d-flex news-empty-listView">
    <div class="ma-auto py-5 d-flex flex-column align-center">
      <i class="uiNoNewsIcon"></i>
      <span class="title listViewTitle mt-5">
        {{ $t('news.latest.noNews') }}
      </span>
    </div>
  </v-flex>
</template>