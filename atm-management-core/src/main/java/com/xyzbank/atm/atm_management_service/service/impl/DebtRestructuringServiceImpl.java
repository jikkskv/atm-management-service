package com.xyzbank.atm.atm_management_service.service.impl;

import com.xyzbank.atm.atm_management_service.dao.DebtBalanceDao;
import com.xyzbank.atm.atm_management_service.debt.DebtBalance;
import com.xyzbank.atm.atm_management_service.debt.DebtStatus;
import com.xyzbank.atm.atm_management_service.service.DebtRestructuringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DebtRestructuringServiceImpl implements DebtRestructuringService {

    @Autowired
    private DebtBalanceDao debtBalanceDao;

    @Override
    public void restructureDebt() {
        List<DebtBalance> debtBalanceList = debtBalanceDao.findByDebtStatus(DebtStatus.PENDING);
        Map<Long, BigDecimal> debtBalanceMap = generateDebtBalanceMap(debtBalanceList);
        List<Long> userList = debtBalanceMap.keySet().stream().toList();
        List<BigDecimal> balanceList = userList.stream().map(debtBalanceMap::get).collect(Collectors.toList());
        List<List<Number>> optimizedTrans = new ArrayList<>();
        optimizeDebtUsingBackTracking(balanceList, 0, userList, optimizedTrans, new ArrayList<>());
        saveOptimizedTrans(debtBalanceList.stream().map(DebtBalance::getDebtId).collect(Collectors.toSet()), optimizedTrans);
    }

    private void saveOptimizedTrans(Set<Long> debtIds, List<List<Number>> optimizedTrans) {
        List<DebtBalance> debtBalanceList = optimizedTrans.stream()
                .map(e -> DebtBalance.buildDebtBalanceObject(e.get(1).longValue(), e.get(0).longValue(), new BigDecimal(String.valueOf(e.get(2))).negate()))
                .collect(Collectors.toList());
        debtBalanceDao.deleteAllById(debtIds);
        debtBalanceDao.saveAllAndFlush(debtBalanceList);
    }

    private Map<Long, BigDecimal> generateDebtBalanceMap(List<DebtBalance> debtBalanceList) {
        if (CollectionUtils.isEmpty(debtBalanceList)) return Collections.emptyMap();
        Map<Long, BigDecimal> debtBalanceMap = new HashMap<>();
        debtBalanceList.forEach(e -> {
            debtBalanceMap.put(e.getFromAccountId(), debtBalanceMap.getOrDefault(e.getFromAccountId(), BigDecimal.ZERO).subtract(e.getOutStandingBalance()));
            debtBalanceMap.put(e.getToAccountId(), debtBalanceMap.getOrDefault(e.getToAccountId(), BigDecimal.ZERO).add(e.getOutStandingBalance()));
        });
        return debtBalanceMap;
    }

    private int optimizeDebtUsingBackTracking(List<BigDecimal> balanceList, int curPos, List<Long> userList, List<List<Number>> result, List<List<Number>> tempResult) {
        while (curPos < balanceList.size() && balanceList.get(curPos).compareTo(BigDecimal.ZERO) == 0) {
            curPos++;
        }
        if (curPos == balanceList.size()) {
            result.clear();
            result.addAll(tempResult);
            return 0;
        }
        int minTransactions = Integer.MAX_VALUE;
        for (int idx = curPos + 1; idx < balanceList.size(); idx++) {
            if (balanceList.get(idx).multiply(balanceList.get(curPos)).compareTo(BigDecimal.ZERO) < 0) {
                tempResult.add(List.of(userList.get(curPos), userList.get(idx), balanceList.get(curPos).abs().min(balanceList.get(idx).abs()).negate()));

                balanceList.set(idx, balanceList.get(idx).add(balanceList.get(curPos)));
                int transactions = 1 + optimizeDebtUsingBackTracking(balanceList, curPos + 1, userList, result, tempResult);
                minTransactions = Math.min(minTransactions, transactions);
                balanceList.set(idx, balanceList.get(idx).subtract(balanceList.get(curPos)));

                tempResult.remove(tempResult.size() - 1);
            }
        }
        return minTransactions;
    }
}
