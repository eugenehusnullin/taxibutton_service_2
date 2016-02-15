package tb.car;

import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import tb.dao.IPartnerDao;
import tb.domain.Partner;

@Service
@EnableScheduling
public class CarOptionsSynch {
	private static final Logger logger = LoggerFactory.getLogger(CarOptionsSynch.class);

	@Autowired
	private IPartnerDao partnerDao;
	@Autowired
	private CarOptionsSynchProcessing processing;
	private ExecutorService execService;

	@PostConstruct
	public void starting() {
		execService = java.util.concurrent.Executors.newFixedThreadPool(5);
	}

	@PreDestroy
	public void stoping() {
		execService.shutdownNow();
	}

	@Scheduled(cron = "0 01 * * * *")
	public void synch() {
		logger.info("Start caroptions synch.");
		List<Partner> partners = partnerDao.getActive();
		for (Partner partner : partners) {
			if (partner.getCustomCarOptions()) {
				execService.execute(new Runnable() {
					@Override
					public void run() {
						processing.makeCarOptionsSynch(partner);
					}
				});
			}
		}
	}

}
