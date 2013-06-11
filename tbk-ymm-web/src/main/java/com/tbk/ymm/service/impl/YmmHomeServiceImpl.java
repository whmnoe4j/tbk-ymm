package com.tbk.ymm.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.tbk.ymm.commons.dto.YmmCateBarDTO;
import com.tbk.ymm.commons.dto.YmmHomeDataOneCate;
import com.tbk.ymm.commons.dto.YmmItem;
import com.tbk.ymm.commons.enums.CateRefType;
import com.tbk.ymm.commons.model.YmmCateRef;
import com.tbk.ymm.commons.model.YmmNavigationCate;
import com.tbk.ymm.dao.YmmCateRefDAO;
import com.tbk.ymm.dao.article.YmmArticleCateDAO;
import com.tbk.ymm.dao.article.YmmArticleDAO;
import com.tbk.ymm.logic.YmmRecommendLogic;
import com.tbk.ymm.service.YmmCateService;
import com.tbk.ymm.service.YmmHomeService;

@Service
public class YmmHomeServiceImpl implements YmmHomeService {

	private final Log logger = LogFactory.getLog(getClass());

	@Autowired
	private YmmCateService ymmCateService;
	@Autowired
	private YmmRecommendLogic ymmRecommendLogic;
	@Autowired
	private YmmArticleCateDAO ymmArticleCateDAO;
	@Autowired
	private YmmArticleDAO ymmArticleDAO;
	@Autowired
	private YmmCateRefDAO ymmCateRefDAO;

	@Override
	public List<YmmHomeDataOneCate> getHomeData(List<YmmNavigationCate> navigationList) {
		List<YmmHomeDataOneCate> homeDataList = initAndSetCateInfo(navigationList);
		for (YmmHomeDataOneCate homeDataOneCate : homeDataList) {
			YmmNavigationCate navCate = homeDataOneCate.getNavigationCate();
			//
			List<YmmItem> itemList = ymmRecommendLogic.getRecommendByNav(navCate,
					YmmHomeDataOneCate.RECOMMEND_ITEM_NUM);
			homeDataOneCate.setItemList(itemList);
		}
		//
		buildArticleInfo(homeDataList);
		//
		logger.debug("YmmHomeServiceImpl.getHomeData:" + homeDataList);
		//
		return homeDataList;
	}

	// -------------------------------------------

	/**
	 * 把每个类目的推荐文章build进首页数据
	 * 
	 * @param homeDataList
	 */
	private void buildArticleInfo(List<YmmHomeDataOneCate> homeDataList) {
		//
		Map<Long, YmmCateRef> mapByCate = ymmCateRefDAO.getAllMapByCateIdAndType(CateRefType.articleCateId);
		for (YmmHomeDataOneCate ymmHomeDataOneCate : homeDataList) {
			YmmNavigationCate navCate = ymmHomeDataOneCate.getNavigationCate();
			if (null == navCate) {
				continue;
			}
			YmmCateRef ymmCateRef = mapByCate.get(navCate.getId());
			if (null == ymmCateRef) {
				continue;
			}
			//
			int articleCateId = ymmCateRef.getArticleCateId();
			//
			ymmHomeDataOneCate.setArticleCateId(articleCateId);
			ymmHomeDataOneCate.setArticleList(ymmArticleDAO.getListByCid(articleCateId, 5));
		}
	}

	/**
	 * 初始化各个类别dto，并放入分类信息；TODO 这个查询比较多，可能需要优化
	 * 
	 * @param homeDataList
	 * @param navigationList
	 */
	private List<YmmHomeDataOneCate> initAndSetCateInfo(List<YmmNavigationCate> navigationList) {
		List<YmmHomeDataOneCate> homeDataList = Lists.newArrayListWithCapacity(navigationList.size());
		for (YmmNavigationCate navigationCate : navigationList) {
			YmmCateBarDTO ymmCateBarDTO = ymmCateService.getCateBar(navigationCate.getId());
			YmmHomeDataOneCate homeDataOneCate = new YmmHomeDataOneCate();
			//
			navigationCate.buildLv2CateList(ymmCateBarDTO.getLv2CateList());
			//
			homeDataOneCate.setNavigationCate(navigationCate);
			homeDataOneCate.setItemCateList(ymmCateBarDTO.getLv2CateList());
			//
			homeDataList.add(homeDataOneCate);
		}
		return homeDataList;
	}

}
