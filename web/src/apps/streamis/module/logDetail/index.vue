<template>
  <div>
    <Modal
      :title="$t('message.streamis.logDetail.logDetail')"
      v-model="visible"
      footer-hide
      width="1200"
      @on-cancel="cancel"
      :class="full ? 'full' : ''"
    >
      <div>
        <Form ref="queryForm" inline>
          <FormItem>
            <Select
              v-model="query.logType"
              @on-change="handleQuery"
              :placeholder="$t('message.streamis.logDetail.logTypeKeywords')"
            >
              <Option v-for="item in logTypeList" :value="item.value" :key="item.value">{{ item.label }}</Option>
            </Select>
          </FormItem>
          <FormItem>
            <Input
              search
              v-model="query.ignoreKeywords"
              :placeholder="$t('message.streamis.logDetail.ignoreKeywords')"
              @on-enter="handleQuery"
            >
            </Input>
          </FormItem>
          <FormItem>
            <Input
              search
              v-model="query.onlyKeywords"
              :placeholder="$t('message.streamis.logDetail.onlyKeywords')"
              @on-enter="handleQuery"
            >
            </Input>
          </FormItem>

          <FormItem>
            <Button
              type="primary"
              @click="handleQuery()"
              style="margin-left: 30px;"
            >
              {{ $t('message.streamis.formItems.queryBtn') }}
            </Button>
          </FormItem>
        </Form>
        <div class="log-wrapper">
          <Input
            v-model="logs"
            type="textarea"
            :autosize="{ minRows: 10, maxRows: 15 }"
            readonly
            :placeholder="$t('message.streamis.logDetail.noLog')"
          />
          <span
            class="full-btn"
            @click="fullToggle"
          >
            {{ full ? '> <' : '< >' }}
          </span>
        </div>
        <div class="btnWrap">
          <Button
            type="primary"
            @click="handleMore('pre')"
            :disabled="fromLine === 1"
          >
            {{ $t('message.streamis.logDetail.pre') }}
          </Button>
          <Button
            type="primary"
            :disabled="toEnd"
            @click="handleMore('next')"
            style="margin-left: 30px;"
          >
            {{ $t('message.streamis.logDetail.next') }}
          </Button>
          <Button
            type="primary"
            @click="handleMore('more')"
            style="margin-left: 30px;"
          >
            {{ $t('message.streamis.logDetail.latestLog') }}
          </Button>
        </div>
        <Spin fix v-if="spinShow"></Spin>
      </div>
    </Modal>
  </div>
</template>
<script>
import api from '@/common/service/api'
export default {
  props: {
    visible: Boolean,
    datas: Array,
    fromHistory: Boolean,
    projectName: String,
    taskId: Number
  },
  data() {
    return {
      query: {
        ignoreKeywords: '',
        onlyKeywords: '',
        logType: 'client'
      },
      logTypeList: [{
        label: this.$t('message.streamis.logDetail.clientLabel'),
        value: 'client'
      }, {
        label: this.$t('message.streamis.logDetail.yarnLabel'),
        value: 'yarn'
      }],
      fromLine: 1,
      endLine: 100,
      toEnd: false,
      logs: '',
      spinShow: false,
      full: false,
    }
  },
  methods: {
    getDatas(type, taskId) {
      // const logs = new Array(1000).fill(
      //   'pps/pps/streamis/module/versionDetailtreamis/module/versionDetailpps/streamis/module/versionDetailpps/streamis/module/versionDetailpps/streamis/module/versionDetailpps/streamis/module/versionDetail'
      // )
      // this.logs = logs.join('\n')
      const { id } = this.$route.params || {}
      let queries = `?jobId=${id}&fromLine=${this.fromLine}&pageSize=100`
      if (taskId || this.taskId) queries += `&taskId=${taskId || this.taskId}`;
      Object.keys(this.query).forEach(key => {
        const value = this.query[key]
        if (value) {
          queries = `${queries}&${key}=${value}`
        }
      })
      this.spinShow = true
      api
        .fetch('streamis/streamJobManager/job/logs' + queries, 'get')
        .then(res => {
          this.spinShow = false
          if (res && res.logs) {
            console.log(this.fromLine, this.endLine)
            if (res.logs.endLine < (this.fromLine + 99)) {
              this.toEnd = true;
            }else{
              this.toEnd = false;
            }
            this.endLine = res.logs.endLine;
            if (res.logs.logs.length > 0) { // 如果有数据，就替换
              this.logs = res.logs.logs.join('\n')
            }
            if (type === 'query') { // 如果是查询时请求该接口，则不管是否有数据都替换内容
              this.logs = res.logs.logs.join('\n')
            }
          } else {
            this.logs = ''
          }
        })
        .catch(e => {
          console.log(e)
          this.logs = ''
          this.spinShow = false
        })
    },
    cancel() {
      this.fromLine = 1
      this.endLine = 100
      this.spinShow = false
      this.toEnd = false;
      this.query = {
        ignoreKeywords: '',
        onlyKeywords: '',
        logType: 'client'
      }
      this.$emit('modalCancel')
      this.full = false
    },
    handleMore(type) {
      if (type === 'more') {
        this.fromLine = 1
        this.query = {
          ...this.query,
          ignoreKeywords: '',
          onlyKeywords: '',
        }
      } else if (type === 'next') {
        this.fromLine = this.fromLine + 100;
      } else {
        this.fromLine = this.fromLine > 100 ? this.fromLine - 100 : 1
      }
      this.getDatas('more')
    },
    handleQuery() {
      this.fromLine = 1
      this.endLine = 100
      this.toEnd = false;
      this.getDatas('query')
    },
    fullToggle(){
      this.full = !this.full
    },
  },
}
</script>
<style lang="scss" scoped>
.btnWrap {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
}
.log-wrapper{
  position: relative;
  .full-btn{
    position: absolute;
    cursor: pointer;
    top: 5px;
    right: 8px;
    font-weight: bold;
  }
}
.full {
  ::v-deep .ivu-modal{
    width: 100vw !important;
    height: 100vh;
    min-height: 430px;
    top:0;
    .ivu-modal-content{
      height: 100%;
    }
  }
  ::v-deep textarea{
   height: calc(100vh - 200px) !important;
   min-height: 200px;
   max-height: calc(100vh - 200px) !important;
  }
}
</style>
