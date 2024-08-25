package com.example.kyc.member.repository;

import com.example.kyc.member.entity.Member;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberRepository {
    private final ObjectMapper mapper;

    @Value("${json.path.members}")
    private String membersJsonPath;
    public Optional<Member> findByEmail(String email) throws IOException {
        Map<String, List<Member>> data = mapper.readValue(new File(membersJsonPath), new TypeReference<Map<String, List<Member>>>() {});

        // Extract the list of members
        List<Member> members = data.get("members");

        Optional<Member> member = members.stream()
                .filter(m -> m.getEmail().equals(email))
                .findFirst();

        return member;
    }

    public void save(Member member) {
        try {
            Map<String, List<Member>> data = mapper.readValue(new File(membersJsonPath), new TypeReference<Map<String, List<Member>>>() {});
            List<Member> members = data.get("members");
            members.add(member);

            Map<String, List<Member>> updatedData = Map.of("members", members);
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(membersJsonPath), updatedData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
