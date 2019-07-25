package com.apigcc.example.jaxrs;

import com.appigs.domain.Result;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 * 学生信息操作接口resource
 *
 * @email eumji025@gmail.com
 * @author:EumJi
 * @date: 2019/3/23
 * @time: 14:16
 */
@Path("/student")
@Produces({"application/json"})
public class StudentResource {

    /**
     * 根据学生Id获取学生信息
     *
     * @param id
     * @return
     */
    @GET
    @Path("{id}/json")
    @Produces("application/json")
    public Result<Student> getStudentXml(@PathParam("id") int id) {
        Result<Student> studentResult = new Result<>();
        studentResult.setObj(new Student());
        return studentResult;
    }

}
