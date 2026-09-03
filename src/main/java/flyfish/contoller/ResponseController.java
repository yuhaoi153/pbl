//package flyfish.contoller;
//
//import flyfish.pojo.Address;
//import flyfish.pojo.Result;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.ArrayList;
//import java.util.List;
////
////@RestController//包含了Controller和Responsebody的两个注解
////public class ResponseController {
////    @RequestMapping("/responseAddr")
////    public Address address(){
////        Address addr = new Address();
////        addr.setProvince("广东");
////        addr.setCity("深圳");
////        return addr;
////    }
////
////    @RequestMapping("/responseList")
////    public List<Address> addrlist(){
////        List<Address> list = new ArrayList<>();//新建一个数组列表，数组列表中的元素是Address类对象
////
////        Address addr = new Address();
////        addr.setProvince("浙江");
////        addr.setCity("温州");
////
////        Address addr2 = new Address();
////        addr2.setProvince("广东");
////        addr2.setCity("深圳");
////
////        list.add(addr);
////        list.add(addr2);
////
////        return list;
////
////    }
////
////}
//
//
//@RestController//包含了Controller和Responsebody的两个注解
//public class ResponseController {
//    @RequestMapping("/responseAddr")
//    public Result address(){
//        Address addr = new Address();
//        addr.setProvince("广东");
//        addr.setCity("深圳");
//        return Result.success(addr);
//    }
//
//    @RequestMapping("/responseList")
//    public Result addrlist(){
//        List<Address> list = new ArrayList<>();//新建一个数组列表，数组列表中的元素是Address类对象
//
//        Address addr = new Address();
//        addr.setProvince("浙江");
//        addr.setCity("温州");
//
//        Address addr2 = new Address();
//        addr2.setProvince("广东");
//        addr2.setCity("深圳");
//
//        list.add(addr);
//        list.add(addr2);
//
//        return Result.success(list);
//
//    }
//
//}
