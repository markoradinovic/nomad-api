package io.github.zanella.nomad.v1;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.damnhandy.uri.template.UriTemplate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import io.github.zanella.nomad.v1.common.models.Constraint;
import io.github.zanella.nomad.v1.common.models.Job;
import io.github.zanella.nomad.v1.common.models.UpdateStrategy;
import io.github.zanella.nomad.v1.jobs.JobApi;
import io.github.zanella.nomad.v1.jobs.models.JobAllocation;
import io.github.zanella.nomad.v1.jobs.models.JobEvalResult;
import io.github.zanella.nomad.v1.jobs.models.JobEvaluation;
import io.github.zanella.nomad.v1.nodes.models.Resources;
import io.github.zanella.nomad.v1.nodes.models.Task;
import io.github.zanella.nomad.v1.nodes.models.TaskGroup;

import org.junit.Test;

public class JobApiTest extends AbstractCommon {
    @Test
    public void getJobTest() {
        final String rawJobResponse = "{" +
                "\"Region\": \"global\"," +
                "\"ID\": \"binstore-storagelocker\"," +
                "\"Name\": \"binstore-storagelocker\"," +
                "\"Type\": \"service\"," +
                "\"Priority\": 50," +
                "\"AllAtOnce\": false," +
                "\"Datacenters\": [\"us2\", \"eu1\"]," +
                "\"Constraints\": [ {\"LTarget\": \"kernel.os\",\"RTarget\": \"windows\",\"Operand\": \"=\"} ]," +
                "\"TaskGroups\": [" +
                "    {" +
                "        \"Name\": \"binsl\"," +
                "        \"Count\": 5," +
                "        \"Constraints\": [ {\"LTarget\": \"kernel.os\",\"RTarget\": \"linux\",\"Operand\": \"=\"} ]," +
                "        \"Tasks\": [" +
                "            {" +
                "                \"Name\": \"binstore\", \"Driver\": \"docker\"," +
                "                \"Config\": {" +
                "                    \"image\": \"hashicorp/binstore\"," +
                "                    \"volumes\": [\"/var/test:/test\"]" +
                "                }," +
                "                \"Constraints\": null," +
                "                \"Resources\": {" +
                "                    \"CPU\": 500,\"MemoryMB\": 0,\"DiskMB\": 0,\"IOPS\": 0," +
                "                    \"Networks\": [ {" +
                "                        \"Device\": \"\", \"CIDR\": \"\", \"IP\": \"\", \"MBits\": 100," +
                "                        \"ReservedPorts\": null,\"DynamicPorts\": null" +
                "                    } ]" +
                "                }," +
                "                \"Meta\": null" +
                "            }," +
                "            {" +
                "                \"Name\": \"storagelocker\",\"Driver\": \"java\"," +
                "                \"Config\": {" +
                "                    \"jar_path\": \"local/test.jar\"," +
                "                    \"jvm_options\": [\"-Xms64m\", \"-Xmx128m\"]" +
                "                }," +
                "                \"Constraints\": [" +
                "                    {\"LTarget\": \"kernel.arch\",\"RTarget\": \"amd64\",\"Operand\": \"=\"}" +
                "                ]," +
                "                \"Resources\": {\"CPU\": 500,\"MemoryMB\": 0,\"DiskMB\": 0,\"IOPS\": 0,\"Networks\": null}," +
                "                \"Meta\": null," +
                "                \"Artifacts\": [ {" +
                "                    \"GetterSource\": \"http://localhost/test.jar\"," +
                "                    \"RelativeDest\": \"local/test\"," +
                "                    \"GetterOptions\": {" +
                "                        \"checksum\": \"md5:c4aa853ad2215426eb7d70a21922e794\"" +
                "                    }" +
                "                } ]" +
                "            }" +
                "        ]," +
                "        \"Meta\": {\"elb_checks\": \"3\",\"elb_interval\": \"10\",\"elb_mode\": \"tcp\"}," +
                "        \"EphemeralDisk\": {\"Sticky\": \"true\",\"Migrate\": \"true\",\"SizeMB\": \"300\"}" +
                "    }" +
                "]," +
                "\"Update\": {\"Stagger\": 0,\"MaxParallel\": 0}," +
                "\"Meta\": {\"foo\": \"bar\"}," +
                "\"Status\": \"\"," +
                "\"StatusDescription\": \"\"," +
                "\"CreateIndex\": 14," +
                "\"ModifyIndex\": 14" +
                "}";

        stubFor(get(urlEqualTo(UriTemplate.fromTemplate(JobApi.jobUrl).expand(ImmutableMap.of("jobId", "jobId"))))
                        .willReturn(aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(rawJobResponse.replace("'", "\""))
                        )
        );

        final Job expectedJob = new Job();

        expectedJob.setRegion("global");
        expectedJob.setId("binstore-storagelocker");
        expectedJob.setName("binstore-storagelocker");
        expectedJob.setType("service");
        expectedJob.setPriority(50);
        expectedJob.setAllAtOnce(false);
        expectedJob.setDatacenters( ImmutableList.of("us2", "eu1") );
        expectedJob.setConstraints( ImmutableList.of(new Constraint("=", "windows", "kernel.os")) );

        final TaskGroup taskGroup = new TaskGroup(
                ImmutableMap.of("elb_checks", "3", "elb_interval", "10", "elb_mode", "tcp"),
                ImmutableList.of(
                        Task.builder()
                            .name("binstore")
                            .resources(new Resources(500, 0, 0, 0, ImmutableList.of(new Resources.Network(null, null, 100, "", "", ""))))
                            .driver("docker")
                            .config(Task.Config.builder()
                                .image("hashicorp/binstore")
                                .volumes(ImmutableList.of("/var/test:/test"))
                            .build())
                        .build(),
                        Task.builder()
                            .resources(new Resources(500, 0, 0, 0, null))
                            .constraints(ImmutableList.of(new Constraint("=", "amd64", "kernel.arch")))
                            .driver("java")
                            .name("storagelocker")
                            .config(Task.Config.builder()
                                .jarPath("local/test.jar")
                                .jvmOptions(ImmutableList.of("-Xms64m", "-Xmx128m"))
                            .build())
                            .artifacts(ImmutableList.of(new Task.Artifacts("http://localhost/test.jar",
                                    "local/test",
                                    ImmutableMap.of("checksum", "md5:c4aa853ad2215426eb7d70a21922e794"))))
                        .build()),
                null,
                ImmutableList.of(new Constraint("=", "linux", "kernel.os")),
                new TaskGroup.EphemeralDisk(true, true, 300),
                4,
                "binsl");

        expectedJob.setTaskGroups( ImmutableList.of(taskGroup) );

        expectedJob.setUpdate( new UpdateStrategy(0, 0d) );
        expectedJob.setMeta( ImmutableMap.of("foo", "bar"));
        expectedJob.setStatus("");
        expectedJob.setStatusDescription("");
        expectedJob.setCreateIndex(14);
        expectedJob.setModifyIndex(14);

        final Job actualJobResult = nomadClient.v1.job.getJob("jobId");

        assertNotEquals(expectedJob, actualJobResult);

        taskGroup.setCount(5);

        assertEquals(expectedJob, actualJobResult);
    }

    @Test
    public void getJobAllocationsTest() {
        final String rawJobAllocations = "[ {" +
                "    \"ID\": \"3575ba9d-7a12-0c96-7b28-add168c67984\"," +
                "    \"EvalID\": \"151accaa-1ac6-90fe-d427-313e70ccbb88\"," +
                "    \"Name\": \"binstore-storagelocker.binsl[0]\"," +
                "    \"NodeID\": \"a703c3ca-5ff8-11e5-9213-970ee8879d1b\"," +
                "    \"JobID\": \"binstore-storagelocker\"," +
                "    \"TaskGroup\": \"binsl\"," +
                "    \"DesiredStatus\": \"run\"," +
                "    \"DesiredDescription\": \"\"," +
                "    \"ClientStatus\": \"running\"," +
                "    \"ClientDescription\": \"\"," +
                "    \"CreateIndex\": 16," +
                "    \"ModifyIndex\": 16," +
                "    \"CreateTime\": 1481729010423779645" +
                "} ]";

        stubFor(get(urlEqualTo(UriTemplate.fromTemplate(JobApi.jobAllocationsUrl).expand(ImmutableMap.of("jobId", "jobId"))))
                        .willReturn(aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(rawJobAllocations.replace("'", "\""))
                        )
        );

        final JobAllocation expectedJobAllocation = new JobAllocation();
        expectedJobAllocation.setId("3575ba9d-7a12-0c96-7b28-add168c67984");
        expectedJobAllocation.setEvalId("151accaa-1ac6-90fe-d427-313e70ccbb88");
        expectedJobAllocation.setName("binstore-storagelocker.binsl[0]");
        expectedJobAllocation.setNodeId("a703c3ca-5ff8-11e5-9213-970ee8879d1b");
        expectedJobAllocation.setJobId("binstore-storagelocker");
        expectedJobAllocation.setTaskGroup("binsl");
        expectedJobAllocation.setDesiredStatus("run");
        expectedJobAllocation.setDesiredDescription("");
        expectedJobAllocation.setClientStatus("running");
        expectedJobAllocation.setClientDescription("");
        expectedJobAllocation.setCreateIndex(16);
        expectedJobAllocation.setModifyIndex(16);
        expectedJobAllocation.setCreateTime(1481729010423779645L);

        assertEquals(ImmutableList.of(expectedJobAllocation), nomadClient.v1.job.getJobAllocations("jobId"));
    }

    @Test
    public void getJobEvaluationsTest() {
        final String rawJobEvaluations = "[ {" +
                "    \"ID\": \"151accaa-1ac6-90fe-d427-313e70ccbb88\"," +
                "    \"Priority\": 50," +
                "    \"Type\": \"service\"," +
                "    \"TriggeredBy\": \"job-register\"," +
                "    \"JobID\": \"binstore-storagelocker\"," +
                "    \"JobModifyIndex\": 14," +
                "    \"NodeID\": \"\"," +
                "    \"NodeModifyIndex\": 0," +
                "    \"Status\": \"complete\"," +
                "    \"StatusDescription\": \"\"," +
                "    \"Wait\": 0," +
                "    \"NextEval\": \"\"," +
                "    \"PreviousEval\": \"\"," +
                "    \"CreateIndex\": 15," +
                "    \"ModifyIndex\": 17" +
                "} ]";

        stubFor(get(urlEqualTo(UriTemplate.fromTemplate(JobApi.jobEvaluationsUrl).expand(ImmutableMap.of("jobId", "jobId"))))
                        .willReturn(aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(rawJobEvaluations.replace("'", "\""))));

        final JobEvaluation expectedJobEvaluation = new JobEvaluation("151accaa-1ac6-90fe-d427-313e70ccbb88", 50,
                "service", "job-register", "binstore-storagelocker", 14, "", 0, "complete", "", 0, "", "", 15, 17);

        assertEquals(ImmutableList.of(expectedJobEvaluation), nomadClient.v1.job.getJobEvaluations("jobId"));
    }

    @Test
    public void putJobEvaluateTest() {
        final String rawEval = "{ \"EvalID\": \"d092fdc0-e1fd-2536-67d8-43af8ca798ac\"," +
                "\"EvalCreateIndex\": 35,\"JobModifyIndex\": 34, \"Index\": 348, \"LastContact\": 0,\n" +
                "\"KnownLeader\": false\n }";

        stubFor(put(urlEqualTo(UriTemplate.fromTemplate(JobApi.jobEvaluateUrl).expand(ImmutableMap.of("jobId", "jobId"))))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(rawEval.replace("'", "\""))));

        assertEquals(new JobEvalResult("d092fdc0-e1fd-2536-67d8-43af8ca798ac", 35, 34, 348, 0, false),
                nomadClient.v1.job.putJobEvaluate("jobId"));
    }

    @Test
    public void deleteJobTest() {
        final String rawEval = "{ \"EvalID\": \"d092fdc0-e1fd-2536-67d8-43af8ca798ac\"," +
                "\"EvalCreateIndex\": 35,\"JobModifyIndex\": 34, \"Index\": 348, \"LastContact\": 0,\n" +
                "\"KnownLeader\": false\n }";

        stubFor(delete(urlEqualTo(UriTemplate.fromTemplate(JobApi.jobUrl).expand(ImmutableMap.of("jobId", "jobId"))))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(rawEval.replace("'", "\""))));

        assertEquals(new JobEvalResult("d092fdc0-e1fd-2536-67d8-43af8ca798ac", 35, 34, 348, 0, false),
                nomadClient.v1.job.deleteJob("jobId"));
    }
}
