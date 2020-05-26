package com.lichao.salesstock.system.controller;

import com.lichao.salesstock.comm.annotation.LogAnnotation;
import com.lichao.salesstock.comm.page.table.PageTableHandler;
import com.lichao.salesstock.comm.page.table.PageTableRequest;
import com.lichao.salesstock.comm.page.table.PageTableResponse;
import com.lichao.salesstock.comm.utils.UserUtil;
import com.lichao.salesstock.system.dao.NoticeDao;
import com.lichao.salesstock.system.dto.NoticeReadVO;
import com.lichao.salesstock.system.dto.NoticeVO;
import com.lichao.salesstock.system.model.Notice;
import com.lichao.salesstock.system.model.SysUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "公告")
@RestController
@RequestMapping("/notices")
public class NoticeController {

    @Autowired
    private NoticeDao noticeDao;

    @LogAnnotation
    @PostMapping
    @ApiOperation(value = "保存公告")
    @PreAuthorize("hasAuthority('notice:add')")
    public Notice saveNotice(@RequestBody Notice notice) {
        noticeDao.save(notice);

        return notice;
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据id获取公告")
    @PreAuthorize("hasAuthority('notice:query')")
    public Notice get(@PathVariable Long id) {
        return noticeDao.getById(id);
    }

    @GetMapping(params = "id")
    public NoticeVO readNotice(Long id) {
        NoticeVO vo = new NoticeVO();

        Notice notice = noticeDao.getById(id);
        if (notice == null || notice.getStatus() == Notice.Status.DRAFT) {
            return vo;
        }
        vo.setNotice(notice);

        noticeDao.saveReadRecord(notice.getId(), UserUtil.getLoginUser().getId());

        List<SysUser> users = noticeDao.listReadUsers(id);
        vo.setUsers(users);

        return vo;
    }

    @LogAnnotation
    @PutMapping
    @ApiOperation(value = "修改公告")
    @PreAuthorize("hasAuthority('notice:add')")
    public Notice updateNotice(@RequestBody Notice notice) {
        Notice no = noticeDao.getById(notice.getId());
        if (no.getStatus() == Notice.Status.PUBLISH) {
            throw new IllegalArgumentException("发布状态的不能修改");
        }
        noticeDao.update(notice);

        return notice;
    }

    @GetMapping
    @ApiOperation(value = "公告管理列表")
    @PreAuthorize("hasAuthority('notice:query')")
    public PageTableResponse listNotice(PageTableRequest request) {
        return new PageTableHandler(new PageTableHandler.CountHandler() {

            @Override
            public int count(PageTableRequest request) {
                return noticeDao.count(request.getParams());
            }
        }, new PageTableHandler.ListHandler() {

            @Override
            public List<Notice> list(PageTableRequest request) {
                return noticeDao.list(request.getParams(), request.getOffset(), request.getLimit());
            }
        }).handle(request);
    }

    @LogAnnotation
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除公告")
    @PreAuthorize("hasAuthority('notice:del')")
    public void delete(@PathVariable Long id) {
        noticeDao.delete(id);
    }

    @ApiOperation(value = "未读公告数")
    @GetMapping("/count-unread")
    public Integer countUnread() {
        SysUser user = UserUtil.getLoginUser();
        return noticeDao.countUnread(user.getId());
    }

    @GetMapping("/published")
    @ApiOperation(value = "公告列表")
    public PageTableResponse listNoticeReadVO(PageTableRequest request) {
        request.getParams().put("userId", UserUtil.getLoginUser().getId());

        return new PageTableHandler(new PageTableHandler.CountHandler() {

            @Override
            public int count(PageTableRequest request) {
                return noticeDao.countNotice(request.getParams());
            }
        }, new PageTableHandler.ListHandler() {

            @Override
            public List<NoticeReadVO> list(PageTableRequest request) {
                return noticeDao.listNotice(request.getParams(), request.getOffset(), request.getLimit());
            }
        }).handle(request);
    }
}
