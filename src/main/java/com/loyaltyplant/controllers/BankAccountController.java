package com.loyaltyplant.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.loyaltyplant.entity.BankAccount;
import com.loyaltyplant.repositories.BankAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/bankAccount")
public class BankAccountController {

    @Autowired
    BankAccountRepository bankAccountRepository;

    @RequestMapping(method = RequestMethod.GET)
    public String index() {
        return "bankAccount/list";
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public DataTablesDTO getBankAccounts(@RequestParam(value = "start", defaultValue = "0") int start,
                                         @RequestParam(value = "length", defaultValue = "10") int length) throws JsonProcessingException {
        Pageable pageable = new PageRequest((start + 1)/length, length);
        DataTablesDTO dto = new DataTablesDTO();
        dto.setLength(length);
        dto.setStart(start);
        dto.setRecordsTotal(bankAccountRepository.count());
        dto.setRecordsFiltered(dto.getRecordsTotal());
        dto.setData(bankAccountRepository.findAll(pageable).getContent());
        return dto;
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST, consumes= MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long save(@RequestBody BankAccount bankAccount) {
        bankAccountRepository.save(bankAccount);
        return bankAccount.getId();
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable Long id) {
        bankAccountRepository.delete(id);
    }

    //DTO-класс для плагина dataTables
    private class DataTablesDTO {

        private int draw;
        private int start;
        private int length;
        private long recordsTotal;
        private long recordsFiltered;
        private List<BankAccount> data;

        public int getDraw() {
            return draw;
        }

        public void setDraw(int draw) {
            this.draw = draw;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public long getRecordsTotal() {
            return recordsTotal;
        }

        public void setRecordsTotal(long recordsTotal) {
            this.recordsTotal = recordsTotal;
        }

        public long getRecordsFiltered() {
            return recordsFiltered;
        }

        public void setRecordsFiltered(long recordsFiltered) {
            this.recordsFiltered = recordsFiltered;
        }

        public List<BankAccount> getData() {
            return data;
        }

        public void setData(List<BankAccount> data) {
            this.data = data;
        }
    }
}
